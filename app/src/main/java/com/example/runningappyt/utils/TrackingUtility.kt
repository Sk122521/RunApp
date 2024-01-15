package com.example.runningappyt.utils

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.example.runningappyt.services.polyline

import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {


    fun hasLocationPermissions(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        }else{
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    fun getFormattedStopWatchTime(ms : Long , includeMillis : Boolean = false ): String {

        var miliseconds = ms
        val hours = TimeUnit.MICROSECONDS.toHours(miliseconds)
        miliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(miliseconds)
        miliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliseconds)
        miliseconds -= TimeUnit.MILLISECONDS.toMillis(seconds)
        miliseconds /= 10


        if (!includeMillis){
            return "${if(hours < 10) "0" else ""}" +
                    "${if(minutes< 10) "0" else ""} $minutes"+
                    "${if(seconds < 10)  "0" else ""}$seconds"+
                    "${if(miliseconds < 10)  "0" else ""}$miliseconds"
        }

        return "  "

     }

    fun calculatePolylineLength(polyline: polyline): Float{
       var distance = 0f
       for (i in 0..polyline.size-2){
           val pos1 = polyline[i]
           val pos2 = polyline[i+1]

           val result = FloatArray(1)
           Location.distanceBetween(
               pos1.latitude,pos1.longitude,pos2.latitude,pos2.longitude,result
           )
           distance += result[0]
       }
        return distance
    }
}