package com.kjooy0305.musiclist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.widget.Toast
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    // 웹의 <input type="file" multiple> 요청을 안드로이드 파일 선택기로 연결
    private val fileChooser = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // parseResult가 일부 기기에서 빈 결과를 돌려줘 clipData/data를 직접 읽는다
        val data = result.data
        val list = ArrayList<Uri>()
        if (result.resultCode == RESULT_OK && data != null) {
            val clip = data.clipData
            if (clip != null) {
                for (i in 0 until clip.itemCount) clip.getItemAt(i).uri?.let { list.add(it) }
            }
            if (list.isEmpty()) data.data?.let { list.add(it) }
        }
        if (list.isEmpty()) {
            Toast.makeText(
                this,
                "선택된 파일이 없어요 (resultCode=${result.resultCode}, data=${data != null}, clip=${data?.clipData?.itemCount})",
                Toast.LENGTH_LONG
            ).show()
        }
        val uris: Array<Uri>? = if (list.isEmpty()) null else list.toTypedArray()
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    private var pendingText: String? = null

    // 웹에서 넘긴 텍스트를 사용자가 고른 위치에 저장 (저장소 권한 불필요)
    private val saveFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val text = pendingText
        pendingText = null
        val uri = result.data?.data
        if (result.resultCode == RESULT_OK && uri != null && text != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                Toast.makeText(this, "저장했어요", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    inner class Bridge {
        @JavascriptInterface
        fun saveText(name: String, text: String) {
            runOnUiThread {
                pendingText = text
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, name)
                }
                try {
                    saveFile.launch(intent)
                } catch (e: Exception) {
                    pendingText = null
                    Toast.makeText(this@MainActivity, "저장 창을 열 수 없어요", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        // https 가상 도메인으로 서비스해야 IndexedDB·DecompressionStream이 안정적으로 동작
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.addJavascriptInterface(Bridge(), "AndroidBridge")

        webView.webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView,
                callback: ValueCallback<Array<Uri>>,
                params: FileChooserParams
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                // 기기에 따라 MIME이 달라 필터가 막힐 수 있어 모든 파일을 보여주고 웹에서 음악 파일만 걸러낸다
                // GET_CONTENT+chooser는 일부 기기에서 결과가 비어 돌아와 OPEN_DOCUMENT 사용
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                return try {
                    fileChooser.launch(intent)
                    true
                } catch (e: Exception) {
                    filePathCallback = null
                    Toast.makeText(this@MainActivity, "파일 선택 창을 열 수 없어요: ${e.message}", Toast.LENGTH_LONG).show()
                    false
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 열린 시트·화면이 있으면 웹에서 먼저 닫고, 아니면 앱을 종료
                webView.evaluateJavascript("(window.onAppBack && window.onAppBack()) === true") { r ->
                    if (r != "true") finish()
                }
            }
        })

        webView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html")
    }

    override fun onDestroy() {
        filePathCallback?.onReceiveValue(null)
        filePathCallback = null
        super.onDestroy()
    }
}
