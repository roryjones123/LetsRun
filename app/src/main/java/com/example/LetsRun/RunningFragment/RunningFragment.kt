package com.example.LetsRun.RunningFragment

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.LetsRun.LogFragment.Run
import com.example.LetsRun.R
import com.example.LetsRun.SaveRunFragment.SaveRunActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDateTime

class RunningFragment : Fragment(), OnMapReadyCallback {
    lateinit var googleMap: GoogleMap

    private lateinit var startButton:Button
    lateinit var stopButton:Button
    lateinit var pauseButton:Button
    lateinit var resumeButton:Button

    lateinit var distanceTextView: TextView
    lateinit var averagePaceTextView: TextView
    lateinit var timeTextView: TextView

    var currentPolyline = ArrayList<LatLng>()
    var allPolylines = ArrayList<List<LatLng>>()

    //stores values of speed etc
    var elapsedTime = 0L
    var distance = 0.0
    var finalSpeed = 0.0

    private lateinit var mMessageReceiver:BroadcastReceiver
    private lateinit var mNotificationMessageReceiver:BroadcastReceiver
    //values for state of service
    private var PAUSE = "1"
    private var START = "2"
    private var STOP = "3"
    private var RESUME = "4"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.running_fragment_activity, container, false)

        setupMap()
        initButtons(rootView)
        initTextViews(rootView)
        setupFirstReceiver()
        setupSecondReceiver()

        return rootView
    }

    //sends message to broadcast receiver in service, pausing or starting activity
    private fun isRunning(currentServiceState: String) {
        val intent = Intent("intentKey2")
        intent.putExtra("key2", currentServiceState)
        context?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(intent) }

        if (currentServiceState == PAUSE) {
            val polylineCopy = currentPolyline.toMutableList()
            allPolylines.add(polylineCopy)
            currentPolyline.clear()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setUpMap()
    }

    //set up google map
    private fun setUpMap() {
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        //more permission checks
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
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

    //formats long in milliseconds to proper time format %02d:%02d:%02d
    fun formatMilsToProperTime(timeToFormat: Long): String {
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

    //creates finished run send to finished run activity for saving/deletion
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createFinishedRun():Run {
        return Run("default name", finalSpeed,distance,elapsedTime,0L, LocalDateTime.now().toString(),allPolylines)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupFinishDialogue() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Are you sure you want to finish this run?")
                .setPositiveButton("Confirm") { dialog, _ ->
                    dialog.cancel()
                    isRunning(STOP)
                    LocalBroadcastManager.getInstance(it)
                        .unregisterReceiver(mMessageReceiver)
                    requireContext().unregisterReceiver(mNotificationMessageReceiver)
                    requireContext().stopService(Intent(context,RunningService::class.java))
                    goToRunFinishedActivity()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun goToRunFinishedActivity() {
        //create run and puts it as intent for next activity
        createFinishedRun()
        val finishedRunIntent = Intent(context,SaveRunActivity::class.java)
        finishedRunIntent.putExtra("FinishedRun",createFinishedRun())
        //put data here
        activity?.finish()
        startActivity(finishedRunIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initButtons(rootView:View) {
        //find buttons
        startButton = rootView.findViewById(R.id.startButton)
        stopButton = rootView.findViewById(R.id.stopButton)
        pauseButton = rootView.findViewById(R.id.pauseButton)
        resumeButton = rootView.findViewById(R.id.resumeButton)

        startButton.setOnClickListener {
            // Service created here -> controlled with this view's buttons that send booleans -> destroyed after stop
            //starts foreground service that tracks location
            context?.startForegroundService(Intent(context, RunningService::class.java))

            startButton.visibility = View.GONE
            pauseButton.visibility = View.VISIBLE
        }

        pauseButton.setOnClickListener {
            //pauses location updates, statistics
            pauseButton.visibility = View.GONE
            resumeButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
            isRunning(PAUSE)
        }

        resumeButton.setOnClickListener {
            //when resume, this should create a new poly line, and not calculate statistic of paused travel
            pauseButton.visibility = View.VISIBLE
            resumeButton.visibility = View.GONE
            stopButton.visibility = View.GONE
            isRunning(RESUME)
        }

        stopButton.setOnClickListener {
            setupFinishDialogue()
        }
    }

    private fun setupMap() {
        //sets up map
        val mapFragment: SupportMapFragment? =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun initTextViews(rootView: View) {
        distanceTextView = rootView.findViewById(R.id.distanceIntView)
        averagePaceTextView = rootView.findViewById(R.id.averagePaceIntView)
        timeTextView = rootView.findViewById(R.id.durationIntView)
    }

    private fun setupFirstReceiver() {
        //local message receiver for receiving broadcasts from service
        mMessageReceiver= object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                //sets time from received message
                timeTextView.text = formatMilsToProperTime(intent.getLongExtra("time", 0L))
                elapsedTime = intent.getLongExtra("time", 0L)

                //sets distance from received message
                distanceTextView.text = (intent.getDoubleExtra("distance", 0.0)).toString()
                distance = intent.getDoubleExtra("distance", 0.0)

                //sets speed from received speed, checking for divisions by 0
                val speed = intent.getDoubleExtra("averageSpeed", 0.0)
                if (!speed.isNaN() && !speed.isInfinite()) {
                    averagePaceTextView.text =
                        formatMilsToProperTime(speed.toLong())

                }
                finalSpeed = intent.getDoubleExtra("averageSpeed", 0.0)

                //turns received latlng into doubles and converts into latlng object, adds them to list of latlngs
                val latLng = LatLng(
                    intent.getDoubleExtra("currentLatitude", 0.0),
                    intent.getDoubleExtra("currentLongitude", 0.0)
                )

                //adds latlng to a list of latlngs then adds that to map object
                if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                    /*this condition is to stop the default values being sent through
                    and messing up the line. A 0.0 long and lat will very likely never occur
                     */
                    currentPolyline.add(latLng)
                    googleMap.addPolyline(PolylineOptions().addAll(currentPolyline))

                    //camera auto positioning
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                }
            }
        }
        context?.let {
            LocalBroadcastManager.getInstance(it)
                .registerReceiver(mMessageReceiver, IntentFilter("intentKey"))
        }
    }

    private fun setupSecondReceiver() {
        //creating separate receiver for notification broadcasts
        mNotificationMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                //pause
                if (intent.getStringExtra("key2").equals(PAUSE)||intent.getStringExtra("key2").equals(STOP)) {
                    pauseButton.visibility = View.GONE
                    resumeButton.visibility = View.VISIBLE
                    stopButton.visibility = View.VISIBLE

                    //makes sure paused polylines via notifications aren't added to map
                    val polylineCopy = currentPolyline.toMutableList()
                    allPolylines.add(polylineCopy)
                    currentPolyline.clear()

                }

                if (intent.getStringExtra("key2").equals(RESUME)) {
                    pauseButton.visibility = View.VISIBLE
                    resumeButton.visibility = View.GONE
                    stopButton.visibility = View.GONE
                }
            }
        }
        context?.registerReceiver(mNotificationMessageReceiver, IntentFilter("notificationAction"))
    }
}
