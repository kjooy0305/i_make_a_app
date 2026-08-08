package com.kjooy.novelwiki

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val CHANNEL_ID = "novelwiki_reminders"

    // 알림 권한 요청 결과를 JS 쪽 콜백으로 전달
    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val result = if (isGranted) "granted" else "denied"
        webView.post {
            webView.evaluateJavascript(
                "if(window.__notifPermCallback){window.__notifPermCallback('$result');window.__notifPermCallback=null;}",
                null
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        webView = findViewById(R.id.webview)

        // API 33+ 기기에서 앱 첫 실행 시 알림 권한 미리 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        // JS ↔ Android 알림 브릿지 등록
        webView.addJavascriptInterface(NotificationBridge(), "AndroidNotifBridge")

        webView.webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

            override fun onPageFinished(view: WebView, url: String) {
                // Service Worker 해제
                view.evaluateJavascript("""
                    if ('serviceWorker' in navigator) {
                        navigator.serviceWorker.getRegistrations()
                            .then(regs => regs.forEach(r => r.unregister()));
                    }
                """.trimIndent(), null)

                // WebView 안에서 Notification API가 동작하지 않으므로
                // Android 네이티브 알림으로 연결하는 폴리필 주입
                view.evaluateJavascript("""
                    (function() {
                        var bridge = window.AndroidNotifBridge;
                        if (!bridge) return;

                        function NotifPolyfill(title, opts) {
                            if (NotifPolyfill.permission === 'granted') {
                                bridge.showNotification(
                                    title,
                                    (opts && opts.body)  || '',
                                    (opts && opts.icon)  || ''
                                );
                            }
                        }

                        NotifPolyfill.permission = bridge.getPermission();

                        NotifPolyfill.requestPermission = function() {
                            return new Promise(function(resolve) {
                                var p = NotifPolyfill.permission;
                                if (p === 'granted' || p === 'denied') {
                                    resolve(p);
                                    return;
                                }
                                window.__notifPermCallback = function(result) {
                                    NotifPolyfill.permission = result;
                                    resolve(result);
                                };
                                bridge.requestPermission();
                            });
                        };

                        Object.defineProperty(window, 'Notification', {
                            value: NotifPolyfill,
                            configurable: true,
                            writable: true
                        });
                        // 폴리필 주입 후 ReminderEngine 재기동 (부트 타이밍 누락 복구)
                        if (window.ReminderEngine) {
                            window.ReminderEngine.start();
                        }
                    })();
                """.trimIndent(), null)
            }
        }

        webView.settings.apply {
            javaScriptEnabled      = true
            domStorageEnabled      = true
            databaseEnabled        = true
            allowFileAccess        = false
            allowContentAccess     = false
            setSupportZoom(false)
            builtInZoomControls    = false
            displayZoomControls    = false
        }

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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "소설 창작위키 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "리마인더 및 작성 독려 알림"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    inner class NotificationBridge {

        @JavascriptInterface
        fun getPermission(): String {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return "granted"
            return when {
                ActivityCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> "granted"
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                ) -> "default"
                else -> "denied"
            }
        }

        @JavascriptInterface
        fun requestPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                runOnUiThread {
                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        @JavascriptInterface
        fun syncReminders(json: String) {
            ReminderScheduler.saveAndScheduleAll(applicationContext, json)
        }

        @JavascriptInterface
        fun syncWritingReminder(json: String) {
            ReminderScheduler.saveAndScheduleWriting(applicationContext, json)
        }

        @JavascriptInterface
        fun updateWritingStatus(hasWritten: Boolean, date: String) {
            applicationContext.getSharedPreferences("novelwiki_reminders", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("written_today", hasWritten)
                .putString("written_date", date)
                .apply()
        }

        @JavascriptInterface
        fun showNotification(title: String, body: String, icon: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return

            val notif = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(this@MainActivity)
                .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif)
        }
    }
}
