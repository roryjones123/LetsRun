package com.example.LetsRun.OldRunActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.LetsRun.LogFragment.Run
import com.example.LetsRun.MainActivity
import com.example.LetsRun.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class OldRunActivity:AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private var oldRunViewModel: OldRunViewModel? = null
    private lateinit var run:Run

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.run_in_log)
        oldRunViewModel = ViewModelProvider(this).get(OldRunViewModel::class.java)

        //gets run from intent
        getRun()
        initTextViews()
        initButtons()

        //observes view model, reacts to deletion
        oldRunViewModel!!.wasDeleted.observe(
            this,
            Observer { value ->
                if(value) {
                    goBackToMainIntent()
                    Toast.makeText(this,"Run deleted!",Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this,"Run was not deleted!",Toast.LENGTH_LONG).show()
                }
            }
        )

        //code for instantiating map
        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    //below code adds stored polyline from firestore to map
    override fun onMapReady(map: GoogleMap?) {
        if (map != null) {
            googleMap = map
            val run = intent.getParcelableExtra("runToView") as Run?
            if (run != null) {
                for(value in run.polyLineList) {
                    googleMap.addPolyline(PolylineOptions().addAll(value))
                }
                if(run.polyLineList.isNotEmpty()) {
                    if(run.polyLineList[0].isNotEmpty()) {
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                run.polyLineList[0][0],
                                18f
                            )
                        )
                    }
                }
            }
        }
        setUpMap()
    }

    //simple map setup with more permission checks
    private fun setUpMap() {
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        //more permission checks
        if (applicationContext?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && applicationContext?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap.isMyLocationEnabled = true
    }

    private fun goBackToMainIntent() {
        startActivity(Intent(applicationContext,MainActivity::class.java))
    }

    //formats time to 6 digits, takes up white space and looks good
    private fun formatMilsToProperTime(timeToFormat: Long): String {
        return String.format(
            "%02d:%02d:%02d", timeToFormat / 3600,
            timeToFormat % 3600 / 60, timeToFormat % 60
        )
    }

    //formats time, but this code formats to 4 digits if first 2 values are 0.
    private fun formatMilsToProperTimeWithoutZeros(timeToFormat: Long): String {
        val formattedTime = String.format(
            "%02d:%02d:%02d", timeToFormat / 3600,
            timeToFormat % 3600 / 60, timeToFormat % 60
        )
        //for removing/adding hours if time < 1 hour
        return if (formattedTime[0] == '0' && formattedTime[1] == '0') {
            formattedTime.substring(3, formattedTime.length)
        } else if (formattedTime[0] == '0' && formattedTime[1] != '0') {
            formattedTime.substring(1, formattedTime.length)
        } else {
            formattedTime
        }
    }

    //splits the localdatetime tostring stored in firestore and returns time section
    private fun getStartTime(date:String):String {
        return date.split("T")[1].split(".")[0]
    }

    //sets up discard dialogue
    private fun setupDiscardDialogue(run:Run) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Are you sure you want to discard this run?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.cancel()
                oldRunViewModel!!.deleteRun(run.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun initTextViews() {
        //gets text views
        val durationTextView = findViewById<TextView>(R.id.durationChangeableTextView)
        val distanceTextView = findViewById<TextView>(R.id.distanceTextViewChangeable)
        val calorieTextView = findViewById<TextView>(R.id.calorieTextViewChangeable)
        val paceTextView = findViewById<TextView>(R.id.avgPaceTextViewChangeable)
        val startTimeTextView = findViewById<TextView>(R.id.startTimeTextViewChangeable)

        //sets text view values
        durationTextView.text = formatMilsToProperTime(run.elapsedTime)
        distanceTextView.text = run.distance.toString() + " km"
        calorieTextView.text = run.calories.toString() + " kcal"
        paceTextView.text = formatMilsToProperTimeWithoutZeros(run.averagePace.toLong()) + " km/h"
        startTimeTextView.text = getStartTime(run.dateCreated)
    }

    private fun getRun() {
        run = (intent.getParcelableExtra("runToView") as Run?)!!
    }

    private fun initButtons() {
        val backButton = findViewById<ImageView>(R.id.backButton)
        val discardButton = findViewById<ImageView>(R.id.discardButton)

        backButton.setOnClickListener {
            goBackToMainIntent()
        }

        discardButton.setOnClickListener {
            setupDiscardDialogue(run)
        }
    }
}