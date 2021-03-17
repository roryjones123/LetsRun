package com.example.LetsRun.RunningFragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.LetsRun.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.math.BigDecimal
import java.math.RoundingMode

class RunningService : Service() {

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    //for controlling count up timer
    var previousTimeElapsed = 0L
    private lateinit var elapsedTimer: CountDownTimer

    private lateinit var receiver: BroadcastReceiver

    //value for polylines sent to RunningFragment, previous values for calculating distance
    var currentLat = 0.0
    var currentLong = 0.0
    var previousLat = 0.0
    var previousLong = 0.0

    //distance value
    var distance = 0.0

    //time value
    var elapsedTime = 0L

    //values for state of service
    //var isRunning = false
    var serviceCurrentState = "2"
    var PAUSE = "1"
    var START = "2"
    var STOP = "3"
    var RESUME = "4"

    //need to be top level, so callback can be removed in onDestroy()
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    //notification constants
    val NOTIFICATION_ID = 101
    private var CHANNEL_ID = "My Notification"

    //for custom notification
    lateinit var notification: Notification
    lateinit var notificationLayout: RemoteViews
    lateinit var notificationManager: NotificationManager

    //top level binder variable
    private val myBinder = MyLocalBinder()

    //binds service
    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        createChannelId()
        startForegroundNotification()
        createAndStartTimer()
        initiateHandlerAndRunnable()
        setupReceiver()
        setUpLocationCallback()
        checkPermissions()
    }

    /*method for creating a timer, using the stored previousTimeElapsed value to
    save the timer between pauses*/
    fun createTimer(): CountDownTimer {
        return object : CountDownTimer(1000000000, 100) {
            var startTime = System.currentTimeMillis() / 1000
            var totalTime = "0:00:00"
            override fun onTick(l: Long) {
                elapsedTime = System.currentTimeMillis() / 1000 - startTime + previousTimeElapsed
                totalTime = String.format(
                    "%02d:%02d:%02d", elapsedTime / 3600,
                    elapsedTime % 3600 / 60, elapsedTime % 60
                )
            }

            override fun onFinish() {
                cancel()
                //update the custom notification views to mimic the data being sent to frontend
                //get notification manager to be notified
                previousTimeElapsed = elapsedTime
            }
        }
    }

    //local binder class for binder creation
    inner class MyLocalBinder : Binder()

    //sends message to broadcast receiver in activity, MVVM be damned,
    //this is sent on every clock tick
    private fun sendMessageToActivity() {
        val intent = Intent("intentKey")
        intent.putExtra("time", elapsedTime)
        intent.putExtra("currentLongitude", currentLong)
        intent.putExtra("currentLatitude", currentLat)
        intent.putExtra(
            "averageSpeed",
            (elapsedTime / (distance / 1000))
        ) // fixes units, make sure distance/1000

        /*returns rounded value to 2dp for distance, when it reaches a multiple of .1, it loses a zero
        e.g 0.89 0.9 0.91*/
        intent.putExtra("distance", round(distance / 1000, 2))

        //update the custom notification views to mimic the data being sent to frontend
        //get notification manager to be notified
        notificationLayout.setTextViewText(
            R.id.timeNotificationText,
            "Time:   ${formatMilsToProperTime(elapsedTime)}"
        )
        notificationLayout.setTextViewText(
            R.id.distanceNotificationText,
            "Distance:   ${round((distance / 1000), 2)}"
        )
        notificationManager.notify(NOTIFICATION_ID, notification)
        //sends intent with info to RunningFragment
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    //rounds double to custom dp
    private fun round(value: Double, places: Int): Double {
        require(places >= 0)
        var bd: BigDecimal = BigDecimal.valueOf(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    //nukes the service, cancelling timer/location callbacks before
    override fun onDestroy() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)
        elapsedTimer.cancel()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    //creates notification, taking a string which determine if pause or play
    private fun createNotification(): Notification {
        //create custom view
        notificationLayout = RemoteViews(packageName, R.layout.notification_small)

        //pause notification intent
        val pauseNotificationIntent =
            Intent(applicationContext, NotificationActionService::class.java)
        pauseNotificationIntent.action = PAUSE
        val pauseNotificationPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            1,
            pauseNotificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        //play notification intent
        val playNotificationIntent =
            Intent(applicationContext, NotificationActionService::class.java)
        playNotificationIntent.action = RESUME
        val playNotificationPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            1,
            playNotificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationLayout.setOnClickPendingIntent(
            R.id.notificationPauseButton,
            pauseNotificationPendingIntent
        )
        notificationLayout.setOnClickPendingIntent(
            R.id.notificationPlayButton,
            playNotificationPendingIntent
        )

        return NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)
            .setSmallIcon(R.drawable.run_tab_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.drawable.run_tab_icon
                )
            )
            .addAction(R.drawable.run_tab_icon, "BUTTON", pauseNotificationPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle(distance.toString())
            .setContentText(distance.toString())
            .setSound(null)
            .build()
    }

    //build a notification channel, to avoid bad notification error on Android API>O
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(chan)
        return channelId
    }

    private fun formatMilsToProperTime(timeToFormat: Long): String {
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

    private fun createChannelId() {
        CHANNEL_ID =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                ""
            }
    }

    private fun startForegroundNotification() {
        notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createAndStartTimer() {
        /*this needs to be done when the service is created.
        Service and activity both operate on main thread, the attempt
        to send a boolean to the service on the same button click as starting the service
        won't work, the boolean will be sent ahead of the receiver creation.*/
        elapsedTimer = createTimer()
        elapsedTimer.start()
    }

    @SuppressLint("MissingPermission")
    private fun setUpLocationCallback() {
        //get locationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //this is for setting interval of location request, and other details
        val mLocationRequest = LocationRequest()
        mLocationRequest.maxWaitTime = 1000
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                //The last location in the list is the newest
                val location = locationResult.locations[0]
                Log.i(
                    "MapsActivity",
                    "Location: " + location.latitude + " " + location.longitude
                )
                previousLat = currentLat
                previousLong = currentLong

                currentLat = location.latitude
                currentLong = location.longitude

                if (serviceCurrentState != PAUSE && previousLat != 0.0) {
                    /*the second condition in the above if statement is to stop the app from calculating the massive
                    distance that acrues when initialising the location.*/
                    //this computes data between the lat lang points
                    distance += SphericalUtil.computeDistanceBetween(
                        LatLng(previousLat, previousLong),
                        LatLng(currentLat, currentLong)
                    )
                }
            }
        }
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun checkPermissions() {
        //permissions, should be sorted already but checks if the user denied the request
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                applicationContext,
                "Please allow location permissions!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
    }

    private fun initiateHandlerAndRunnable() {
        //sends value every second
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                sendMessageToActivity()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun setupReceiver() {
        //creates receiver for isRunning boolean, helps with pausing run
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {

                // Get Boolean sent from fragment
                serviceCurrentState = intent.getStringExtra("key2")!!

                if (serviceCurrentState == RESUME) {
                    //notification changer
                    notificationLayout.setViewVisibility(R.id.notificationPauseButton, View.VISIBLE)
                    notificationLayout.setViewVisibility(R.id.notificationPlayButton, View.GONE)
                    notificationManager.notify(NOTIFICATION_ID, notification)

                    //post runnable again
                    handler.post(runnable)

                    elapsedTimer = createTimer()
                    elapsedTimer.start()
                }

                if (serviceCurrentState == PAUSE) {
                    elapsedTimer.onFinish()
                    //remove runnable
                    handler.removeCallbacks(runnable)

                    //notification changer
                    notificationLayout.setViewVisibility(R.id.notificationPauseButton, View.GONE)
                    notificationLayout.setViewVisibility(R.id.notificationPlayButton, View.VISIBLE)
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }

                if (serviceCurrentState == STOP) {
                    onDestroy()
                }
            }
        }
        applicationContext?.let {
            LocalBroadcastManager.getInstance(it)
                .registerReceiver(receiver, IntentFilter("intentKey2"))
        }
        applicationContext?.registerReceiver(receiver, IntentFilter("notificationAction"))
    }
}