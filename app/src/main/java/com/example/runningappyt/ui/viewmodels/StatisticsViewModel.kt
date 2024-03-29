package com.example.runningappyt.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.runningappyt.repository.MainRepository
import javax.inject.Inject

class StatisticsViewModel @ViewModelInject constructor(val mainRepository: MainRepository) : ViewModel(){

    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance  = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runSortedByDate = mainRepository.getAllRunSortedByDate()

}