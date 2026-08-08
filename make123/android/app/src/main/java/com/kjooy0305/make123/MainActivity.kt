package com.kjooy0305.make123

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kjooy0305.make123.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = fileChooserCallback ?: return@registerForActivityResult
        fileChooserCallback = null
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            callback.onReceiveValue(if (uri != null) arrayOf(uri) else arrayOf())
        } else {
            callback.onReceiveValue(arrayOf())
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupWebView()
        b.webView.loadUrl("file:///android_asset/index.html")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val wv = b.webView
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
        }

        wv.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")

        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith("file://")) return false
                // 외부 링크는 브라우저로
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return true
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    toast("페이지 로드 오류: ${error.description}")
                }
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                fileChooserCallback?.onReceiveValue(arrayOf())
                fileChooserCallback = filePathCallback
                val intent = fileChooserParams.createIntent()
                intent.type = "image/*"
                fileChooserLauncher.launch(intent)
                return true
            }

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                result.confirm()
                toast(message)
                return true
            }

            override fun onConsoleMessage(msg: ConsoleMessage): Boolean = true
        }

        // 다운로드 처리 (텍스트 파일 내보내기)
        wv.setDownloadListener { url, _, contentDisposition, _, _ ->
            val filename = URLUtil.guessFileName(url, contentDisposition, null)
            if (url.startsWith("data:")) {
                saveDataUri(url, filename)
            } else {
                toast("파일 저장: $filename")
            }
        }
    }

    private fun saveDataUri(dataUri: String, filename: String) {
        try {
            val base64 = dataUri.substringAfter(",")
            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val cv = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)!!
                resolver.openOutputStream(uri)!!.use { it.write(bytes) }
                cv.clear()
                cv.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, cv, null, null)
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir.mkdirs()
                FileOutputStream(File(dir, filename)).use { it.write(bytes) }
            }
            toast("다운로드 완료: $filename")
        } catch (e: Exception) {
            toast("저장 실패: ${e.message}")
        }
    }

    override fun onBackPressed() {
        val wv = b.webView
        val url = wv.url ?: ""
        if (url.contains("#/") && !url.endsWith("#/") && !url.endsWith("#")) {
            wv.evaluateJavascript("Router.navigate('/')", null)
        } else if (wv.canGoBack()) {
            wv.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    inner class AndroidBridge(private val ctx: Context) {
        @JavascriptInterface
        fun showToast(msg: String) = toast(msg)

        @JavascriptInterface
        fun getAppVersion(): String = "1.0"
    }
}
