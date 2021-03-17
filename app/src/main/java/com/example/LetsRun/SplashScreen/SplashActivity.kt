package com.example.LetsRun.SplashScreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.LetsRun.Authentication.AuthenticationActivity
import com.example.LetsRun.Authentication.User
import com.example.LetsRun.MainActivity
import com.example.LetsRun.Utils.Constants.Companion.USER


class SplashActivity: AppCompatActivity() {
    private var splashViewModel: SplashViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSplashViewModel()
        checkIfUserIsAuthenticated()
    }

    private fun initSplashViewModel() {
        splashViewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
    }

    private fun checkIfUserIsAuthenticated() {
        splashViewModel?.checkIfUserIsAuthenticated()
        splashViewModel!!.isUserAuthenticatedLiveData!!.observe(this,
            Observer { user: User ->
                if (!user.isAuthenticated) {
                    goToAuthInActivity()
                    finish()
                } else {
                    getUserFromDatabase(user.userId)
                }
            }
        )
    }

    private fun getUserFromDatabase(uid: String) {
        splashViewModel?.setUid(uid)
        splashViewModel?.userLiveData?.observe(
            this,
            Observer { user: User? ->
                goToMainActivity(user!!)
                finish()
            }
        )
    }

    private fun goToAuthInActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
    }

    private fun goToMainActivity(user: User) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(USER, user)
        startActivity(intent)
    }
}