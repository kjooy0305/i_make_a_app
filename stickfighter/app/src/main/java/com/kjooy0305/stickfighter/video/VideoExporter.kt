package com.kjooy0305.stickfighter.video

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.model.Interpolator
import com.kjooy0305.stickfighter.render.Fit
import com.kjooy0305.stickfighter.render.FrameRenderer
import java.io.File

enum class Quality(val label: String, val width: Int) {
    QVGA("320×180", 320),
    SD("640×360", 640),
    HD("1280×720 (720p)", 1280),
    FULL_HD("1920×1080 (1080p)", 1920)
}

/** 애니메이션을 MP4(H.264)로 인코딩한다. 그리기는 인코더의 입력 Surface에 직접 한다. */
object VideoExporter {

    class Result(val file: File, val uri: Uri?)

    /** onProgress는 0..1. isCancelled가 true가 되면 중단하고 CancellationException을 던진다. */
    fun export(
        context: Context,
        animation: Animation,
        quality: Quality,
        onProgress: (Float) -> Unit,
        isCancelled: () -> Boolean
    ): Result {
        val width = even(quality.width)
        val height = even((quality.width * animation.ratio).toInt())
        val fps = animation.fps.coerceIn(5, 60)
        val total = ((animation.durationSeconds * fps).toInt() + 1).coerceAtLeast(2)

        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "videos").apply { mkdirs() }
        val file = File(dir, safeName(animation.title) + "_" + System.currentTimeMillis() + ".mp4")

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, (width * height * fps * 0.12f).toInt().coerceIn(500_000, 16_000_000))
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        var muxer: MediaMuxer? = null
        var started = false
        try {
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()
            muxer = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var track = -1
            val info = MediaCodec.BufferInfo()
            val renderer = FrameRenderer()
            val fit = Fit.of(width.toFloat(), height.toFloat(), animation)
            val lastKey = (animation.frames.size - 1).toFloat()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val pixels = IntArray(width * height)

            // 인코더 출력을 비워 muxer에 쓴다. endOfStream이면 끝 신호가 올 때까지 기다린다.
            fun drain(endOfStream: Boolean) {
                while (true) {
                    val index = codec.dequeueOutputBuffer(info, if (endOfStream) 10_000 else 0)
                    when {
                        index == MediaCodec.INFO_TRY_AGAIN_LATER -> if (!endOfStream) return
                        index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            track = muxer!!.addTrack(codec.outputFormat)
                            muxer!!.start()
                            started = true
                        }
                        index >= 0 -> {
                            val data = codec.getOutputBuffer(index)!!
                            if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) info.size = 0
                            if (info.size > 0 && started) {
                                data.position(info.offset)
                                data.limit(info.offset + info.size)
                                muxer!!.writeSampleData(track, data, info)
                            }
                            codec.releaseOutputBuffer(index, false)
                            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                        }
                    }
                }
            }

            fun nextInput(): Int {
                while (true) {
                    val index = codec.dequeueInputBuffer(10_000)
                    if (index >= 0) return index
                    drain(false)
                    if (isCancelled()) throw java.util.concurrent.CancellationException()
                }
            }

            for (i in 0 until total) {
                if (isCancelled()) throw java.util.concurrent.CancellationException()
                val position = lastKey * i / (total - 1)
                renderer.draw(canvas, animation, Interpolator.at(animation.frames, position), fit)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                val index = nextInput()
                fillImage(codec.getInputImage(index)!!, pixels, width, height)
                codec.queueInputBuffer(index, 0, width * height * 3 / 2, i * 1_000_000L / fps, 0)
                drain(false)
                onProgress((i + 1).toFloat() / total)
            }
            val last = nextInput()
            codec.queueInputBuffer(last, 0, 0, total * 1_000_000L / fps, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            drain(true)
            bitmap.recycle()
        } catch (e: Throwable) {
            file.delete()
            throw e
        } finally {
            runCatching { codec.stop() }
            codec.release()
            runCatching { if (started) muxer?.stop() }
            muxer?.release()
        }
        return Result(file, publish(context, file))
    }

    /** Android 10 이상에서는 갤러리(영화 폴더)에도 보이도록 복사한다. */
    private fun publish(context: Context, file: File): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/StickFighter")
            }
            val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)!!
            context.contentResolver.openOutputStream(uri)!!.use { out -> file.inputStream().use { it.copyTo(out) } }
            uri
        }.getOrNull()
    }

    /** ARGB 픽셀을 인코더가 요구하는 YUV420 평면 배치(스트라이드 포함)로 옮긴다. */
    private fun fillImage(image: Image, pixels: IntArray, width: Int, height: Int) {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val yBuf = yPlane.buffer
        val uBuf = uPlane.buffer
        val vBuf = vPlane.buffer
        for (y in 0 until height) {
            for (x in 0 until width) {
                val c = pixels[y * width + x]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                yBuf.put(y * yPlane.rowStride + x * yPlane.pixelStride, (((66 * r + 129 * g + 25 * b + 128) shr 8) + 16).toByte())
                if (y % 2 == 0 && x % 2 == 0) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    uBuf.put((y / 2) * uPlane.rowStride + (x / 2) * uPlane.pixelStride, u.coerceIn(0, 255).toByte())
                    vBuf.put((y / 2) * vPlane.rowStride + (x / 2) * vPlane.pixelStride, v.coerceIn(0, 255).toByte())
                }
            }
        }
    }

    private fun even(v: Int) = (v.coerceAtLeast(2) / 2) * 2

    private fun safeName(raw: String) = raw.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_").trim('_').ifEmpty { "animation" }
}
