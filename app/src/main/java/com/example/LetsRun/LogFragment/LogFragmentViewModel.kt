package com.example.LetsRun.LogFragment

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.QueryDocumentSnapshot

class LogFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val logFragmentRepository:LogFragmentRepository? = LogFragmentRepository()
    val savedRuns = MutableLiveData<List<Run>>()

    //gets document then maps all runs to run objects
    @RequiresApi(Build.VERSION_CODES.O)
    fun getRuns() {
        logFragmentRepository?.getAllRuns()?.addOnSuccessListener { result ->
            val runList = result.map { snapshot ->
                firestoreToRun(snapshot)
            }
            savedRuns.postValue(runList)
        }
    }

    //maps run to run object
    @RequiresApi(Build.VERSION_CODES.O)
    fun firestoreToRun(snapshot: QueryDocumentSnapshot):Run {
        var runList = listOf<List<LatLng>>()
        if(snapshot.get("Polylines")!=null) {
            val runMap = snapshot.get("Polylines") as HashMap<*, *>
            runList = hashmapToArrayList(runMap)
        }

        val avgPace = snapshot.get("averagePace") as Double
        val calories = snapshot.get("calories") as Long
        val dateCreated = snapshot.get("dateCreated") as String
        val distance = snapshot.get("distance") as Double
        val elapsedTime = snapshot.get("elapsedTime") as Long
        val name = snapshot.get("name") as String

        return Run(name,avgPace,distance,elapsedTime,calories,dateCreated, runList)
    }

    private fun hashmapToArrayList(polylinesMap: HashMap<*, *>):List<List<LatLng>> {

        //list of all polylines to add
        val listOfPolylines = ArrayList<List<LatLng>>()

        //goes through all values in map of polylines
        for(value in polylinesMap) {
            //collection for a single polyline
            val singlePolylineList = ArrayList<LatLng>()

            //these lines of code get to the single polyline ArrayList in the FireStore
            val singlePolylineMapParent = value as Map.Entry<*,*>
            val singlePolylineMapChild = singlePolylineMapParent.value as HashMap<*,*>
            val latLngDoubles = singlePolylineMapChild["Polyline"] as ArrayList<*>

            //goes through the values, turns them into latlong objects and then adds them to the list
            for(latLngDouble in latLngDoubles) {
                val latLng = latLngDouble as HashMap<*, *>
                val lat = latLng["latitude"]!!
                val long = latLng["longitude"]!!
                singlePolylineList.add(LatLng(lat as Double, long as Double))
            }

            listOfPolylines.add(singlePolylineList)
        }

        return listOfPolylines
    }
}