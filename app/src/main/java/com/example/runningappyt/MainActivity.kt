package com.example.runningappyt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningappyt.R
import com.example.runningappyt.databinding.ActivityMainBinding
import com.example.runningappyt.db.RunDAO
import com.example.runningappyt.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var activityNewsBinding : ActivityMainBinding ? = null
    val binding get() = activityNewsBinding!!


    @Inject
    lateinit var runDAO: RunDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToTrackingIfNedded(intent)

        activityNewsBinding = ActivityMainBinding.inflate(layoutInflater)

        //setSupportActionBar(toolbar)
        binding.bottomNavigationView.setupWithNavController(binding.fragmentContainerView.findNavController())
        binding.bottomNavigationView.setOnNavigationItemReselectedListener {
             /* NO-OP */
        }

        binding.fragmentContainerView.findNavController()
            .addOnDestinationChangedListener{_,destination,_ ->
              when(destination.id){
                  R.id.settingsFragment,R.id.runFragment,R.id.statisticFragment  ->
                      binding.bottomNavigationView.visibility = View.VISIBLE
                  else -> binding.bottomNavigationView.visibility = View.GONE
              }
        }

    }
    private fun navigateToTrackingIfNedded(intent : Intent?){
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            binding.fragmentContainerView.findNavController().navigate(R.id.action_global_tracking_fragment)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingIfNedded(intent)
    }
}