package com.example.runningappyt.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runningappyt.db.Runningdatabase
import com.example.runningappyt.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningappyt.utils.Constants.KEY_NAME
import com.example.runningappyt.utils.Constants.KEY_WEIGHT
import com.example.runningappyt.utils.Constants.RUNNING_DATABASE_NAME
import com.example.runningappyt.utils.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationCoponent::cl ass)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningdatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        Runningdatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun providesRunDao(db: Runningdatabase) = db.getRunDao()


    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_NAME, "") ?: ""

    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT, 80f)

    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE, true)

}