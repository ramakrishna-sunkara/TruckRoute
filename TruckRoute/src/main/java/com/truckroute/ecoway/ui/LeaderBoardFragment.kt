package com.truckroute.ecoway.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.truckroute.ecoway.R
import com.truckroute.ecoway.adapaters.LeaderBoardListAdapter
import com.truckroute.ecoway.models.LeaderBoardModel
import kotlinx.android.synthetic.main.fragment_leader_board.*

class LeaderBoardFragment : Fragment() {

    private lateinit var leaderBoardModelList: List<LeaderBoardModel>

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
    }

    private fun initViews() {
        readModel()
        val leaderBoardListAdapter = LeaderBoardListAdapter(leaderBoardModelList)
        rvLeaderBoardList.adapter = leaderBoardListAdapter
    }

    private fun readModel() {
        val jsonFileString = getJsonDataFromAsset(requireContext(), "leaderboard.json")
        val gson = Gson()
        val listPersonType = object : TypeToken<List<LeaderBoardModel>>() {}.type
        leaderBoardModelList = gson.fromJson(jsonFileString, listPersonType)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leader_board, container, false)
    }
}