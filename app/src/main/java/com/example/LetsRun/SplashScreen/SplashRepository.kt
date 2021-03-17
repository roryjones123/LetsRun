package com.example.LetsRun.SplashScreen

import androidx.lifecycle.MutableLiveData
import com.example.LetsRun.Authentication.User
import com.example.LetsRun.Utils.Constants.Companion.USERS
import com.example.LetsRun.Utils.HelperClass.Companion.logErrorMessage
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class SplashRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var user: User = User("","","")
    private val rootRef = FirebaseFirestore.getInstance()
    private val usersRef = rootRef.collection(USERS)

    fun checkIfUserIsAuthenticatedInFirebase(): MutableLiveData<User>? {
        val isUserAuthenticateInFirebaseMutableLiveData: MutableLiveData<User> =
            MutableLiveData<User>()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            user.isAuthenticated = false
            isUserAuthenticateInFirebaseMutableLiveData.setValue(user)
        } else {
            user.userId = firebaseUser.uid
            user.isAuthenticated = true
            isUserAuthenticateInFirebaseMutableLiveData.setValue(user)
        }
        return isUserAuthenticateInFirebaseMutableLiveData
    }

    fun addUserToLiveData(uid: String?): MutableLiveData<User?> {
        val userMutableLiveData: MutableLiveData<User?> = MutableLiveData<User?>()
        usersRef.document(uid!!).get()
            .addOnCompleteListener { userTask: Task<DocumentSnapshot?> ->
                if (userTask.isSuccessful) {
                    val document = userTask.result

                    if (document!!.exists()) {
                        val user: User? = document.toObject(User::class.java)
                        userMutableLiveData.value = user
                    }
                } else {
                    userTask.exception!!.message?.let { logErrorMessage(it) }
                }
            }

        return userMutableLiveData
    }
}