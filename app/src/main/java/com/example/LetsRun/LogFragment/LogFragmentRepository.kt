package com.example.LetsRun.LogFragment

import com.example.LetsRun.Utils.Constants.Companion.RUNS
import com.example.LetsRun.Utils.Constants.Companion.USERS
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot


class LogFragmentRepository  {
    val firestoreDb = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    fun getAllRuns(): Task<QuerySnapshot> {
        //this reorders the runs descending so they show up better in the log
        return user?.uid?.let { firestoreDb.collection(USERS).document(user!!.uid).collection(RUNS) }!!.orderBy("dateCreated", Query.Direction.DESCENDING).get()
    }
}