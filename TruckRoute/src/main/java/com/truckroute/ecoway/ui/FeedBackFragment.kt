package com.truckroute.ecoway.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.truckroute.ecoway.R
import kotlinx.android.synthetic.main.fragment_feedback.*

class FeedBackFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btnFeedbackSubmit.setOnClickListener {
            doSubmitFeedback();
        }
        txtBack.setOnClickListener {
            doResetFeedback();
        }
    }

    private fun doResetFeedback() {
        llFeedBackContainer.visibility = View.VISIBLE
        llThankYouContainer.visibility = View.GONE
        rbFeedback.rating = 0f
        edtComments.text.clear()
        edtEmail.text.clear()
    }

    private fun doSubmitFeedback() {
        llFeedBackContainer.visibility = View.GONE
        llThankYouContainer.visibility = View.VISIBLE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            FeedBackFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}