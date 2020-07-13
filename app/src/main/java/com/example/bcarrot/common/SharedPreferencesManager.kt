package com.example.bcarrot.common

import android.content.Context
import android.content.SharedPreferences


object SharedPreferencesManager {
    private const val APP_SETTINGS_FILE = "APP_SETTINGS"
    private val sharedPreferences: SharedPreferences
        private get() = MyApp.context!!.getSharedPreferences(
            APP_SETTINGS_FILE,
            Context.MODE_PRIVATE
        )

    fun setSomeIntValue(dataLabel: String?, dataValue: Int) {
        val editor =
            sharedPreferences.edit()
        editor.putInt(dataLabel, dataValue)
        editor.commit()
    }

    fun setSomeStringValue(dataLabel: String?, dataValue: String?) {
        val editor =
            sharedPreferences.edit()
        editor.putString(dataLabel, dataValue)
        editor.commit()
    }

    fun getSomeIntValue(dataLabel: String?): Int {
        return sharedPreferences.getInt(dataLabel, 0)
    }

    fun getSomeStringValue(dataLabel: String?): String? {
        return sharedPreferences.getString(dataLabel, null)
    }
}