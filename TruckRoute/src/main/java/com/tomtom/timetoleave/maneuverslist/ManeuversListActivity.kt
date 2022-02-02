package com.tomtom.timetoleave.maneuverslist

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.tomtom.online.sdk.routing.route.information.Instruction
import com.tomtom.timetoleave.Constants
import com.tomtom.timetoleave.R
import com.tomtom.timetoleave.adapaters.ManeuversListAdapter
import com.tomtom.timetoleave.driving.tracking.ChevronTrackingFragment
import kotlinx.android.synthetic.main.activity_countdown.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_maneuvers_list.*
import kotlinx.android.synthetic.main.activity_maneuvers_list.imgBack

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
        imgBack.setOnClickListener { finish() }
        if (instructions.isEmpty()){
            txtEmptyList.visibility = View.VISIBLE
            rvManeuversList.visibility = View.GONE
        }else {
            txtEmptyList.visibility = View.GONE
            rvManeuversList.visibility = View.VISIBLE
            maneuversListAdapter = ManeuversListAdapter(instructions)
            rvManeuversList.adapter = maneuversListAdapter
        }
    }

    private fun readArgs() {
        if (intent == null)
            return
        intent.getSerializableExtra(Constants.EXTRA_INSTRUCTIONS).also { instructions = it as List<Instruction> }
    }
}
