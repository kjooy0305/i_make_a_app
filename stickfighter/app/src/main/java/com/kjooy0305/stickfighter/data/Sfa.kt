package com.kjooy0305.stickfighter.data

import com.kjooy0305.stickfighter.model.Animation
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** .sfa 백업 파일. animation.json 하나를 담은 zip이다. */
object Sfa {
    const val ENTRY = "animation.json"
    const val EXTENSION = "sfa"

    fun write(animation: Animation, out: OutputStream) {
        val zip = ZipOutputStream(out)
        zip.putNextEntry(ZipEntry(ENTRY))
        zip.write(Json.toJson(animation).toString().toByteArray(Charsets.UTF_8))
        zip.closeEntry()
        zip.finish()
        zip.flush()
    }

    /** 가져온 애니메이션은 기존 것과 겹치지 않도록 새 id를 받는다. */
    fun read(input: InputStream): Animation {
        val zip = ZipInputStream(input)
        while (true) {
            val entry = zip.nextEntry ?: throw IllegalArgumentException("animation.json이 없는 파일입니다")
            if (entry.name == ENTRY) {
                val text = zip.readBytes().toString(Charsets.UTF_8)
                return Json.animationFrom(JSONObject(text)).copy(id = UUID.randomUUID().toString())
            }
        }
    }
}
