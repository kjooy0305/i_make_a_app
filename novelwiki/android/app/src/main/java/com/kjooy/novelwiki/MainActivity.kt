package com.kjooy.novelwiki

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        // assets/ 폴더를 https://appassets.androidplatform.net/assets/ 로 서빙
        // → IndexedDB·ES모듈이 정상 동작하는 HTTPS 오리진 확보
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

            override fun onPageFinished(view: WebView, url: String) {
                // Service Worker는 앱 내에 번들되어 있어 불필요 → 해제
                view.evaluateJavascript("""
                    if ('serviceWorker' in navigator) {
                        navigator.serviceWorker.getRegistrations()
                            .then(regs => regs.forEach(r => r.unregister()));
                    }
                """.trimIndent(), null)
            }
        }

        webView.settings.apply {
            javaScriptEnabled      = true
            domStorageEnabled      = true   // localStorage
            databaseEnabled        = true   // IndexedDB 보조
            allowFileAccess        = false  // AssetLoader 사용으로 불필요
            allowContentAccess     = false
            setSupportZoom(false)
            builtInZoomControls    = false
            displayZoomControls    = false
        }

        // 뒤로가기: 웹뷰 히스토리 → 앱 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        webView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html")
    }
}
