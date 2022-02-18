package com.truckroute.ecoway.ui

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tomtom.online.sdk.common.util.StringUtils
import com.truckroute.ecoway.R
import kotlinx.android.synthetic.main.fragment_about_us.*

class AboutUsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about_us, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textview_help_description.text = Html.fromHtml(getString(R.string.about_app), Html.FROM_HTML_MODE_COMPACT);
        textview_help_description.movementMethod = LinkMovementMethod.getInstance();
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AboutUsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}