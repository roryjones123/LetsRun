package com.example.LetsRun.Authentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.LetsRun.MainActivity
import com.example.LetsRun.R
import com.example.LetsRun.Utils.Constants.Companion.RC_SIGN_IN
import com.example.LetsRun.Utils.Constants.Companion.USER
import com.example.LetsRun.Utils.HelperClass.Companion.logErrorMessage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

class AuthenticationActivity : AppCompatActivity() {
    private var authViewModel: AuthenticationViewModel? = null
    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)
        initSignInButton()
        initAuthViewModel()
        initGoogleSignInClient()
    }

    private fun initSignInButton() {
        val googleSignInButton =
            findViewById<SignInButton>(R.id.google_sign_in_button)
        googleSignInButton.setOnClickListener { v: View? -> signIn() }
    }

    private fun initGoogleSignInClient() {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun signIn() {
        if(googleSignInClient!=null) {
            val signInIntent: Intent = googleSignInClient!!.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val googleSignInAccount = task.getResult(
                    ApiException::class.java
                )
                if (googleSignInAccount != null) {
                    getGoogleAuthCredential(googleSignInAccount);
                }
            } catch (e: ApiException) {
                e.message?.let { logErrorMessage(it) }
            }
        }
    }


    private fun getGoogleAuthCredential(googleSignInAccount: GoogleSignInAccount) {
        val googleTokenId = googleSignInAccount.idToken
        val googleAuthCredential =
            GoogleAuthProvider.getCredential(googleTokenId, null)
        signInWithGoogleAuthCredential(googleAuthCredential)
    }

    private fun initAuthViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)
    }

    private fun signInWithGoogleAuthCredential(googleAuthCredential: AuthCredential) {
        authViewModel?.signInWithGoogle(googleAuthCredential)
        authViewModel!!.authenticatedUserLiveData!!.observe(this,
            Observer { authenticatedUser: User ->
                if (authenticatedUser.isNew) {
                    createNewUser(authenticatedUser)
                } else {
                    goToMainActivity(authenticatedUser)
                }
            }
        )
    }

    private fun createNewUser(authenticatedUser: User) {
        authViewModel?.createUser(authenticatedUser)
        authViewModel!!.createdUserLiveData!!.observe(
            this,
            Observer { user: User ->
                if (user.isCreated) {
                    toastMessage(user.name!!)
                }
                goToMainActivity(user)
            }
        )
    }

    private fun toastMessage(name: String) {
        Toast.makeText(
            this,
            "Hi $name!\nYour account was successfully created.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun goToMainActivity(user: User) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(USER, user)
        startActivity(intent)
        finish()
    }
}