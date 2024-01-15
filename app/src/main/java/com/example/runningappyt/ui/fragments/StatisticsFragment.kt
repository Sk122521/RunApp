package com.example.runningappyt.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningappyt.R
import com.example.runningappyt.databinding.FragmentSetUpBinding
import com.example.runningappyt.databinding.FragmentStatisticsBinding
import com.example.runningappyt.ui.viewmodels.MainViewModel
import com.example.runningappyt.ui.viewmodels.StatisticsViewModel
import com.example.runningappyt.utils.CustomMarkerView
import com.example.runningappyt.utils.TrackingUtility
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val viewModel : StatisticsViewModel by viewModels()

    private var _binding : FragmentStatisticsBinding? = null
    private val binding  get()   = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setUpBarChart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater,container,false)

        return binding.root
    }

    private fun subscribeToObservers(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let{
                val totalTimeRun  = TrackingUtility.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text = totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let{
                val km  = it/1000
                val totalDistance  = round(km*10f)/10f
                val totalDistanceString  = "${totalDistance} Km"
                binding.tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let{
                val avgSpeed  = round(it* 10f) /10f
                val avgSpeedString = "${avgSpeed} km/h"
                binding.tvAverageSpeed.text = avgSpeedString
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let{
                val totalCaloriesBurned  = "${it}Kcal"
                binding.tvTotalCalories.text = totalCaloriesBurned
            }
        })
        viewModel.runSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let{
             val allAvgSpeed = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
             val barDataSet = BarDataSet(allAvgSpeed, "Avg Speed Over Time").apply {
                 valueTextColor = Color.WHITE
                 color = ContextCompat.getColor(requireContext(), pub.devrel.easypermissions.R.color.colorAccent)
             }
                binding.barChart.data = BarData(barDataSet)
                binding.barChart.marker = CustomMarkerView(it.reversed(),requireContext(), R.layout.marker_view)
                binding.barChart.invalidate()
            }
        })
    }

    private fun setUpBarChart(){
        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
       binding.barChart.apply {
           description.text = "Avg speed Over Time"
           legend.isEnabled = false
       }
    }

}