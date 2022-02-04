package com.truckroute.ecoway

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.truckroute.ecoway.driving.tracking.ChevronTrackingFragment
import kotlinx.android.synthetic.main.activity_login.*

class LiveNavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_navigation)
        addFragmentToActivity(ChevronTrackingFragment())
    }

    private fun addFragmentToActivity(fragment: Fragment?) {
        if (fragment == null) return
        val fm = supportFragmentManager
        val tr = fm.beginTransaction()
        tr.add(R.id.map_view, fragment)
        tr.commitAllowingStateLoss()
    }
}
