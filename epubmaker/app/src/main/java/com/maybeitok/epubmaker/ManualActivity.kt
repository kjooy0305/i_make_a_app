package com.maybeitok.epubmaker

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.maybeitok.epubmaker.databinding.ActivityManualBinding

class ManualActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManualBinding

    override fun attachBaseContext(newBase: Context) {
        val lang = AppPreferences(newBase).language
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.manual)

        val lang = AppPreferences(this).language
        val asset = if (lang == "en") "manual_en.html" else "manual_ko.html"

        binding.webView.settings.javaScriptEnabled = false
        binding.webView.loadUrl("file:///android_asset/$asset")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
