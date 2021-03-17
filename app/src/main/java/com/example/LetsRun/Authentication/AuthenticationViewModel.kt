package com.example.LetsRun.Authentication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.AuthCredential





class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {
    private var authRepository: AuthenticationRepository? = AuthenticationRepository()
    var authenticatedUserLiveData: LiveData<User>? = null
    var createdUserLiveData: LiveData<User>? = null

    fun signInWithGoogle(googleAuthCredential: AuthCredential?) {
        authenticatedUserLiveData = authRepository?.firebaseSignInWithGoogle(googleAuthCredential)
    }

    fun createUser(authenticatedUser: User?) {
        createdUserLiveData = authenticatedUser?.let {
            authRepository?.createUserInFirestoreIfNotExists(
                it
            )
        }
    }
}