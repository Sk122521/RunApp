package com.example.runningappyt.ui.fragments

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runningappyt.R
import com.example.runningappyt.databinding.FragmentSetUpBinding
import com.example.runningappyt.databinding.FragmentTrackingBinding
import com.example.runningappyt.db.Run
import com.example.runningappyt.services.TrackingService
import com.example.runningappyt.services.polyline
import com.example.runningappyt.services.polylines
import com.example.runningappyt.ui.viewmodels.MainViewModel
import com.example.runningappyt.ui.viewmodels.StatisticsViewModel
import com.example.runningappyt.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.runningappyt.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningappyt.utils.Constants.ACTION_STOP_SERVICE
import com.example.runningappyt.utils.Constants.MAP_ZOOM
import com.example.runningappyt.utils.Constants.POLYLINE_COLOR
import com.example.runningappyt.utils.Constants.POLYLINE_WIDTH
import com.example.runningappyt.utils.TrackingUtility
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import kotlin.math.round


const val CANCEL_DIALOG_TRACKING_TAG = "Cancel Dialog"

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private var _binding : FragmentTrackingBinding? = null
    private val binding  get()   = _binding!!


    private val viewModel : MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<polyline>()


    private var map : GoogleMap ? = null

    private var currentTimeInMillis = 0L

    private var menu : Menu? = null

    private var weight = 80f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
        binding.mapView.getMapAsync {
            map = it
            addAllPolyLine()
        }
        subscribeToObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTrackingBinding.inflate(inflater,container,false)
         setHasOptionsMenu(true )
        binding.mapView.onCreate(savedInstanceState)

        binding.btnToggleRun.setOnClickListener {
           toggleRun()
        }

        if (savedInstanceState != null){
            val canceltrackingDialog  = parentFragmentManager.findFragmentByTag(
                CANCEL_DIALOG_TRACKING_TAG) as CancelTrackingDialog?
            canceltrackingDialog.setYesListener {
                stopRun()
            }
        }

        binding.mapView.getMapAsync {
            map = it
            addAllPolyLine()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
        subscribeToObserver()


        return binding.root
    }


    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun sendCommandToService(action : String) =
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun addLatestPolyLine(){
      if (pathPoints.isNotEmpty() && pathPoints.last().size > 1){
          val preLastLatLng = pathPoints.last()[pathPoints.last().size -1]
          val lastLatLng = pathPoints.last().last()

          val polylineOptions = PolylineOptions()
              .color(POLYLINE_COLOR)
              .width(POLYLINE_WIDTH)
              .add(preLastLatLng)
              .add(lastLatLng)
          map?.addPolyline(polylineOptions)
      }


    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints){
            for (pos in polyline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb(){
         map?.snapshot {bmp ->
             var distanceInMeters  = 0
             for (polyline in pathPoints){
                 distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
             }
             val avgSpeed = round((distanceInMeters / 1000f) /(currentTimeInMillis /1000/60/60) * 10) / 10f
             val dateTimeStamp  = Calendar.getInstance().timeInMillis
             val caloriesBurned  = ((distanceInMeters / 1000f) * weight).toInt()
             val run  = Run(bmp,dateTimeStamp, avgSpeed, distanceInMeters, currentTimeInMillis, caloriesBurned)
             viewModel.insertRun(run)
             Snackbar.make(
                 requireActivity().findViewById(R.id.rootView),"Run Saved Successfully",Snackbar.LENGTH_LONG
             ).show()
             stopRun()
         }
    }

    private fun addAllPolyLine(){
        for (polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

   private fun moveCameraToUser(){
       if (pathPoints.isNotEmpty() && pathPoints.last().size > 1){
           map?.animateCamera(
               CameraUpdateFactory.newLatLngZoom(
                   pathPoints.last().last(),
                   MAP_ZOOM
               )
           )
       }
   }

    private fun updateTracking(isTracking : Boolean){
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis > 0){
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        }else if(isTracking){
            binding.btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun subscribeToObserver(){
        TrackingService.isTracking.observe(viewLifecycleOwner,Observer{
            updateTracking(it!!)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it!!
            addLatestPolyLine()
            moveCameraToUser()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it!!
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis,true)
            binding.tvTimer.text = formattedTime
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelCard -> {
                showCancelTrackingDialog()
            }
        }

       return  super.onOptionsItemSelected(item)

    }

    private fun showCancelTrackingDialog(){
       CancelTrackingDialog().apply {
           setYesListener {
               stopRun()
           }
       }.show(parentFragmentManager,CANCEL_DIALOG_TRACKING_TAG)
    }

    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }


}



