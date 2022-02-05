package com.truckroute.ecoway

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.truckroute.ecoway.Constants.*
import kotlinx.android.synthetic.main.activity_webview.*


class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pageType = intent.getStringExtra("type")
        setContentView(R.layout.activity_webview)
        setUpWebView(pageType)
    }

    private fun setUpWebView(pageType: String?) {
        val webViewSetting: WebSettings = webView.settings
        webViewSetting.javaScriptEnabled = true
        webView.webViewClient = AppWebViewClient()
        webView.loadUrl(if (pageType == MENU_PERMIT) PERMIT_URL else FOOD_BANK_URL)
    }

    private class AppWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
            view.loadUrl(url)
            return true
        }
    }
}
