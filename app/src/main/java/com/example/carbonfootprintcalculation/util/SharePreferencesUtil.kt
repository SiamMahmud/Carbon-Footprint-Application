package com.example.carbonfootprintcalculation.util

import android.content.Context
import java.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharePreferencesUtil@Inject constructor(@ApplicationContext context: Context) :
    ISharedPreferencesUtil {


    private val sharedPref = context.getSharedPreferences("com.example.carbonfootprintcalculation.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)


    private fun setString(key: String, value: String?) {
        val editor = sharedPref.edit()
        editor.putString(encryptString(key), value)
        value?.let{
            editor.apply() } ?: editor.remove(key)
    }

    private fun getString(key: String, defValue: String): String {
        return sharedPref.getString(encryptString(key), defValue)!!
    }

    private fun encryptString(input: String?): String? {
        input?.let {
            return Base64.getEncoder().encodeToString(input.toByteArray())
        } ?: run {
            return null
        }
    }

    override fun logout() {
        setAuthToken("")
    }

    override fun getAuthToken(): String? {
        with(getString(AUTH_TOKEN, "")) {
            if (isNullOrEmpty())
                return null
            else return this
        }
    }

    override fun setAuthToken(token: String) {
        setString(AUTH_TOKEN, token)
    }

    companion object {
        const val AUTH_TOKEN = "auth_token"
    }
}