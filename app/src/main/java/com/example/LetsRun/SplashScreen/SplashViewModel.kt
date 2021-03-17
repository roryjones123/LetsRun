package com.example.LetsRun.SplashScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.LetsRun.Authentication.User


class SplashViewModel(application: Application) :AndroidViewModel(application) {
    private var splashRepository = SplashRepository()
    var isUserAuthenticatedLiveData: MutableLiveData<User>? = MutableLiveData()
    var userLiveData: MutableLiveData<User?>? = MutableLiveData()

    fun checkIfUserIsAuthenticated() {
        isUserAuthenticatedLiveData = splashRepository.checkIfUserIsAuthenticatedInFirebase()
    }

    fun setUid(uid: String?) {
        userLiveData = splashRepository.addUserToLiveData(uid)
    }
}