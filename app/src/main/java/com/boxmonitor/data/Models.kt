package com.boxmonitor.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

data class UptimeRecord(
    val date: String,
    val startTime: String,
    val endTime: String?,
    val durationMinutes: Long
)

data class ScreenshotFile(
    val filePath: String,
    val timestamp: Long,
    val dateFormatted: String,
    val timeFormatted: String,
    val sizeKb: Long
)

object AppPreferences {
    private const val PREFS_NAME = "BoxMonitorPrefs"
    private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
    private const val KEY_START_TIME = "start_time"
    private const val KEY_PROJECTION_TOKEN = "projection_token"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isMonitoringEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_MONITORING_ENABLED, true)
    }

    fun setMonitoringEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_MONITORING_ENABLED, enabled).apply()
    }

    fun getStartTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_START_TIME, 0L)
    }

    fun setStartTime(context: Context, time: Long) {
        getPrefs(context).edit().putLong(KEY_START_TIME, time).apply()
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
