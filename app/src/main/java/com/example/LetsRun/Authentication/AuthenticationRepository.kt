package com.example.LetsRun.Authentication

import androidx.lifecycle.MutableLiveData
import com.example.LetsRun.Utils.Constants.Companion.USERS
import com.example.LetsRun.Utils.HelperClass.Companion.logErrorMessage
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class AuthenticationRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val rootRef = FirebaseFirestore.getInstance()
    private val usersRef = rootRef.collection(USERS)

    /*
    takes an authentication credential and uses it on a firebase authinstance that listens
    for result. If successful, gets a boolean on whether or not is new user, assigns user value
    from auth to class, if it isn't null, gets values of all firebase users variables and creates
    a user using our user class and assigns the new user boolean to it, finally setting live
    data value as this new user POJO, and returns live data
     */
    fun firebaseSignInWithGoogle(googleAuthCredential: AuthCredential?): MutableLiveData<User>? {
        val authenticatedUserMutableLiveData: MutableLiveData<User> = MutableLiveData<User>()
        firebaseAuth.signInWithCredential(googleAuthCredential!!)
            .addOnCompleteListener { authTask: Task<AuthResult> ->
                if (authTask.isSuccessful) {
                    val isNewUser =
                        authTask.result!!.additionalUserInfo!!.isNewUser
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        val uid = firebaseUser.uid
                        val name = firebaseUser.displayName
                        val email = firebaseUser.email
                        val user = User(
                            uid,
                            name,
                            email
                        )
                        user.isNew = isNewUser
                        authenticatedUserMutableLiveData.value = user
                    }
                } else {
                    authTask.exception!!.message?.let { logErrorMessage(it) }
                }
            }
        return authenticatedUserMutableLiveData
    }

    /*
    If user does not exist, adds data to firebase
     */
    fun createUserInFirestoreIfNotExists(authenticatedUser: User):MutableLiveData<User> {
        val newUserMutableLiveData = MutableLiveData<User>()
        val uidRef =
            usersRef.document(authenticatedUser.userId)
        uidRef.get()
            .addOnCompleteListener { uidTask: Task<DocumentSnapshot?> ->
                if (uidTask.isSuccessful) {
                    val document = uidTask.result
                    if (!document!!.exists()) {
                        uidRef.set(authenticatedUser)
                            .addOnCompleteListener { userCreationTask: Task<Void?> ->
                                if (userCreationTask.isSuccessful) {
                                    authenticatedUser.isCreated = true
                                    newUserMutableLiveData.setValue(authenticatedUser)
                                } else {
                                    userCreationTask.exception!!.message?.let { logErrorMessage(it) }
                                }
                            }
                    } else {
                        newUserMutableLiveData.setValue(authenticatedUser)
                    }
                } else {
                    uidTask.exception!!.message?.let { logErrorMessage(it) }
                }
            }
        return newUserMutableLiveData
    }
}