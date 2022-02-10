package com.truckroute.ecoway.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.truckroute.ecoway.R
import kotlinx.android.synthetic.main.fragment_web_view.*


class WebViewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var webUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            webUrl = it.getString(Constants.WEB_URL)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Enable Javascript
        val webSettings: WebSettings = web_view.settings
        webSettings.javaScriptEnabled = true
        // Force links and redirects to open in the WebView instead of in a browser
        web_view.webViewClient = AppWebViewClients(progress_circular)
        webUrl?.let {
            web_view.loadUrl(it)
        }
    }

    class AppWebViewClients(private val progressBar: ProgressBar) : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            progressBar.visibility = View.GONE
        }

        init {
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(webUrl: String) =
            WebViewFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.WEB_URL, webUrl)
                }
            }
    }
}