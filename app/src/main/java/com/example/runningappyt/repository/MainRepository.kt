package com.example.runningappyt.repository

import com.example.runningappyt.db.Run
import com.example.runningappyt.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(val runDAO: RunDAO)  {

    suspend fun insertRun (run: Run) = runDAO.insertRun(run)

    suspend fun deleteRun (run: Run) = runDAO.deleteRun(run)

    fun getAllRunSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunSortedByDistance() = runDAO.getAllRunsSortedByDistance()

    fun getAllRunSortedByTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis()

    fun getAllRunSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeed()

    fun getAllRunSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBiurned()

    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()

    fun getTotalDistance() = runDAO.getTotalDistance()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()

}