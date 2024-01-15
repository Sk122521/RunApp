package com.example.runningappyt.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.runningappyt.databinding.FragmentRunBinding
import com.example.runningappyt.databinding.ItemRunBinding
import com.example.runningappyt.databinding.MarkerViewBinding
import com.example.runningappyt.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CustomMarkerView(
    val runs : List<Run>,
    c : Context,
    layoutId: Int
) : MarkerView(c,layoutId) {

    val binding =  MarkerViewBinding.inflate(LayoutInflater.from(c),
        parent as ViewGroup?,false)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)

        if (e == null){
            return
        }
        val curRunId  = e.x.toInt()
        val run  = runs[curRunId]

        val calender  = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }

        val dateFormat  = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calender.time)

        val avgSpeed = "${run.avgSpeedInKMH} Km/h"
        binding.tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f} KM"
        binding.tvDistance.text = distanceInKm

        binding.tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned  = "${run.caloriesBurned} kcal"
        binding.tvCaloriesBurned.text = caloriesBurned

    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f , -height.toFloat())
    }

}