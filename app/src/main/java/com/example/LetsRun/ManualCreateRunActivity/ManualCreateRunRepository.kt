package com.example.LetsRun.ManualCreateRunActivity

import com.example.LetsRun.LogFragment.Run
import com.example.LetsRun.Utils.Constants
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManualCreateRunRepository {
    private val firestoreDb = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    fun addNewRun(runToAdd: Run): Task<Void> {
        val reference = firestoreDb.collection(Constants.USERS).document(user!!.uid)
            .collection(Constants.RUNS).document(runToAdd.toString())

        //adds new run
        return reference.set(runToAdd)
    }
}