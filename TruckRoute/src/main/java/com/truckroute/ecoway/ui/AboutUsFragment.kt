package com.truckroute.ecoway.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.truckroute.ecoway.R

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

    companion object {
        @JvmStatic
        fun newInstance() =
            AboutUsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}