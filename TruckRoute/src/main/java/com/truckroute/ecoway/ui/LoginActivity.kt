package com.truckroute.ecoway.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.truckroute.ecoway.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val USER_ADMIN = "admin"
        private const val USER_TEST = "test"
        private const val USER_EVA = "eva"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btnLogin.setOnClickListener { doLogin() }
    }

    private fun doLogin() {
        val userName = edtUserName.text.toString()
        val password = edtPassword.text.toString()
        if (userName.isNullOrEmpty() || password.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                getString(R.string.empty_creditionals),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if ((userName == USER_ADMIN && password == USER_ADMIN) || (userName == USER_TEST && password == USER_TEST) || (userName == USER_EVA && password == USER_EVA)) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.login_success, userName),
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.invalid_creditionals),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
