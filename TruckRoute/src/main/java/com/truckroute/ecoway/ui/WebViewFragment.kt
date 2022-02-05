package com.truckroute.ecoway.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.truckroute.ecoway.Constants
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
        web_view.loadUrl(webUrl)
        // Enable Javascript
        val webSettings: WebSettings = web_view.settings
        webSettings.javaScriptEnabled = true
        // Force links and redirects to open in the WebView instead of in a browser
        web_view.webViewClient = WebViewClient()
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