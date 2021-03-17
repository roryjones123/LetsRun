package com.example.LetsRun.LogFragment

import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Run @RequiresApi(Build.VERSION_CODES.O) constructor(val name:String = "default name", val averagePace:Double = 0.0, val distance:Double = 0.0, val elapsedTime:Long = 0L, val calories:Long = 0L, val dateCreated:String, @get:Exclude val polyLineList:  List<List<LatLng>> = ArrayList() ):Parcelable {

    override fun toString(): String {
        return "$name $dateCreated"
    }

}