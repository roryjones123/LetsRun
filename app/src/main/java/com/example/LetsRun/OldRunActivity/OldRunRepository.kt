package com.example.LetsRun.OldRunActivity

import com.example.LetsRun.Utils.Constants
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OldRunRepository  {
    private val firestoreDb = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    fun deleteRun(runId:String): Task<Void>? {
        return user?.uid?.let {
            firestoreDb.collection(Constants.USERS).document(user!!.uid).collection(
                Constants.RUNS
            ).document(runId).delete()
        }
    }
}