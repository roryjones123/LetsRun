package com.example.LetsRun.Authentication

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class User(
    var userId: String = "", val name: String? = "", @SuppressWarnings("WeakerAccess") val email: String? = ""
) : Serializable {
    @Exclude
    var isAuthenticated:Boolean = false
    @Exclude
    var isNew:Boolean = false
    @Exclude
    var isCreated:Boolean = false
}