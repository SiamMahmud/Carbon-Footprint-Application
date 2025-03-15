package com.example.carbonfootprintcalculation.util

interface ISharedPreferencesUtil {
    fun logout()
    fun getAuthToken():String?
    fun setAuthToken(token:String)
}