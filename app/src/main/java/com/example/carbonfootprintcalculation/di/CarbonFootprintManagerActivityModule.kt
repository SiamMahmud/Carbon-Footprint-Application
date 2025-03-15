package com.example.carbonfootprintcalculation.di

import android.app.Activity
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object CarbonFootprintManagerActivityModule {
    @Provides
    fun providedMGBActivityUtil(activity: Activity): SMMActivityUtil {
        return SMMActivityUtil(activity as SMMActivityUtil.ActivityListener)
    }
}