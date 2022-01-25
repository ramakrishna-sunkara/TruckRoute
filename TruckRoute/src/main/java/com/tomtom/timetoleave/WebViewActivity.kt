package com.tomtom.timetoleave

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.tomtom.timetoleave.MainActivity.Companion.MENU_PERMIT
import kotlinx.android.synthetic.main.activity_webview.*


class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pageType = intent.getStringExtra("type")
        setContentView(R.layout.activity_webview)
        setUpWebView(pageType)
    }

    private fun setUpWebView(pageType: String?) {
        val permitUrl = "https://coast2coasttruckingpermits.com/state-regulations/"
        val foodBankUrl = "https://www.feedingamerica.org/find-your-local-foodbank"
        val webViewSetting: WebSettings = webView.settings
        webViewSetting.javaScriptEnabled = true
        webView.webViewClient = AppWebViewClient()
        webView.loadUrl(if (pageType == MENU_PERMIT) permitUrl else foodBankUrl)
    }

    private class AppWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
            view.loadUrl(url)
            return true
        }
    }
}
