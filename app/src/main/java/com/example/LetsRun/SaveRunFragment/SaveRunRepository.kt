package com.example.LetsRun.SaveRunFragment

import com.example.LetsRun.LogFragment.Run
import com.example.LetsRun.Utils.Constants.Companion.RUNS
import com.example.LetsRun.Utils.Constants.Companion.USERS
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

class SaveRunRepository {

    private val firestoreDb = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    fun saveRun(run: Run): Task<Void> {
        val reference = firestoreDb.collection(USERS).document(user!!.uid)
            .collection(RUNS).document(run.toString())

        //sets initial values for runs (not polylines)
        reference.set(run)

        //creates a parent and a child map
        val parent = HashMap<String,Any>()
        val child = HashMap<String, Any>()

        //puts polylines in sub-hashmap
        for((counter, list) in run.polyLineList.withIndex()) {
            val map = HashMap<String, Any>()
            val array = list.toTypedArray()
            map["Polyline"] = Arrays.asList(*array)
            child[counter.toString()] = map
        }

        //puts all those maps in one parent map
        parent["Polylines"] = child

        //updates value and returns task
        return reference.update(parent)
    }
}