package com.example.carbonfootprintcalculation

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CarbonFootPrintApplication: Application() {
    companion object {
        @JvmStatic
        fun getApplication(context: Context) = context.applicationContext as CarbonFootPrintApplication
    }
}