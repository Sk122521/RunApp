package com.example.runningappyt.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import com.example.runningappyt.MainActivity
import com.example.runningappyt.R
import com.example.runningappyt.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.runningappyt.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningappyt.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningappyt.utils.Constants.ACTION_STOP_SERVICE
import com.example.runningappyt.utils.Constants.FASTEST_LOCATION_UPDATE
import com.example.runningappyt.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningappyt.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningappyt.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningappyt.utils.Constants.NOTIFICATION_ID
import com.example.runningappyt.utils.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias polyline = MutableList<LatLng>
typealias polylines = MutableList<polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun  = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject
    lateinit var baseNotificationBuilder : NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder


    private val timeRunInSeconds = MutableLiveData<Long>()


    companion object{
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<polylines>()
        val timeRunInMillis = MutableLiveData<Long>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }else{
                       // startForegroundService()
                        startTimer()
                        Timber.d("resuming service")
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("paused service")
                    pauseService()
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("stopped service")
                    killedService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this,Observer{
            if (it != null) {
                updateLocationTracking(it)
                updateNotificationTrackingStatus(it)
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer{
            if (!serviceKilled){
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it!!*1000L ))
                notificationManager.notify(NOTIFICATION_ID,notification.build())
            }
        })

    }

    private fun addEmptyPolylines() = pathPoints.value?.apply {
         add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

   private fun addPathPoints(location: Location){
       location?.let {
           val pos = LatLng(location.latitude,location.longitude)
           pathPoints.value?.apply {
               last().add(pos)
               pathPoints.postValue(this)
           }
       }
   }

    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTracking.value!!){
                result?.locations?.let {locations ->
                    for (location in locations){
                        addPathPoints(location)
                        Timber.d("New Location ${location.latitude }, ${location.longitude}")
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking : Boolean){
        if (isTracking){
            if (TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval =  LOCATION_UPDATE_INTERVAL
                    fastestInterval =  FASTEST_LOCATION_UPDATE
                    priority = PRIORITY_HIGH_ACCURACY
                }

                fusedLocationProviderClient.requestLocationUpdates(
                    request,locationCallback, Looper.getMainLooper()
                )
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timestarted = 0L
    private var lastSecondTimestamp  = 0L

    private fun startTimer(){
        addEmptyPolylines()
        isTracking.postValue(true)
        timestarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.IO).launch {
            while (isTracking.value!!){
                //time difference between now and time started
                lapTime = System.currentTimeMillis() - timestarted
               //post the new laptime
                timeRunInMillis.postValue(timeRun+lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp+1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(50L)
            }
            timeRun += lapTime
        }


    }

   private fun updateNotificationTrackingStatus(isTracking: Boolean){
       val notificationActionText = if (isTracking) "Pause" else "Resume"
       val pendingIntent = if(isTracking){
           val pauseIntent = Intent(this,TrackingService::class.java).apply {
               action = ACTION_PAUSE_SERVICE
           }
           PendingIntent.getService(this,1,pauseIntent, FLAG_UPDATE_CURRENT)
       }else{
           val resumeIntent = Intent(this,TrackingService::class.java).apply {
               action = ACTION_START_OR_RESUME_SERVICE
           }
           PendingIntent.getService(this,2,resumeIntent, FLAG_UPDATE_CURRENT)
       }
       val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)  as NotificationManager

       curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
           isAccessible = true
           set(curNotificationBuilder,ArrayList<NotificationCompat.Action>())
       }

       if (!serviceKilled){
           curNotificationBuilder = baseNotificationBuilder.addAction(
               R.drawable.ic_pause_black_24dp,notificationActionText,pendingIntent
           )
           notificationManager.notify(NOTIFICATION_ID,curNotificationBuilder.build())
       }
   }
    private fun killedService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }
}