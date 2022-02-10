package com.truckroute.ecoway.maneuverslist

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tomtom.online.sdk.routing.route.information.Instruction
import com.truckroute.ecoway.ui.Constants
import com.truckroute.ecoway.R
import com.truckroute.ecoway.adapaters.ManeuversListAdapter
import kotlinx.android.synthetic.main.activity_maneuvers_list.*
import kotlinx.android.synthetic.main.toolbar.*

class ManeuversListActivity : AppCompatActivity() {

    private lateinit var instructions: List<Instruction>
    private lateinit var maneuversListAdapter: ManeuversListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maneuvers_list)
        readArgs()
        initView()
    }

    private fun initView() {
        toolBar.title = getString(R.string.maneuvers_list)
        setSupportActionBar(toolBar)
        toolBar.navigationIcon =
            ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_arrow_back_24)
        toolBar.setNavigationOnClickListener { finish() }
        if (instructions.isEmpty()) {
            txtEmptyList.visibility = View.VISIBLE
            rvManeuversList.visibility = View.GONE
        } else {
            txtEmptyList.visibility = View.GONE
            rvManeuversList.visibility = View.VISIBLE
            maneuversListAdapter = ManeuversListAdapter(instructions)
            rvManeuversList.adapter = maneuversListAdapter
        }
    }

    private fun readArgs() {
        if (intent == null)
            return
        intent.getSerializableExtra(Constants.EXTRA_INSTRUCTIONS)
            .also { instructions = it as List<Instruction> }
    }
}
