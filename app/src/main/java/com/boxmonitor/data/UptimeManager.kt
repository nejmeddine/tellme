package com.boxmonitor.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object UptimeManager {

    private const val PREFS_NAME = "UptimePrefs"
    private const val KEY_SESSIONS = "sessions"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun recordStart(context: Context): Long {
        val now = System.currentTimeMillis()
        AppPreferences.setStartTime(context, now)
        return now
    }

    fun recordStop(context: Context) {
        val startTime = AppPreferences.getStartTime(context)
        if (startTime == 0L) return

        val endTime = System.currentTimeMillis()
        val durationMinutes = (endTime - startTime) / 60000

        val dateStr = AppPreferences.formatDate(startTime)
        val startStr = AppPreferences.formatTime(startTime)
        val endStr = AppPreferences.formatTime(endTime)

        val session = JSONObject().apply {
            put("date", dateStr)
            put("startTime", startStr)
            put("endTime", endStr)
            put("durationMinutes", durationMinutes)
            put("startTimestamp", startTime)
        }

        val prefs = getPrefs(context)
        val existing = prefs.getString(KEY_SESSIONS, "[]")
        val arr = JSONArray(existing)
        arr.put(session)

        // Garder seulement les 90 derniers jours
        val trimmed = JSONArray()
        val cutoff = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.getLong("startTimestamp") > cutoff) {
                trimmed.put(obj)
            }
        }

        prefs.edit().putString(KEY_SESSIONS, trimmed.toString()).apply()
        AppPreferences.setStartTime(context, 0L)
    }

    fun getSessions(context: Context): List<UptimeRecord> {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_SESSIONS, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<UptimeRecord>()

        for (i in arr.length() - 1 downTo 0) {
            val obj = arr.getJSONObject(i)
            list.add(
                UptimeRecord(
                    date = obj.getString("date"),
                    startTime = obj.getString("startTime"),
                    endTime = obj.optString("endTime", null),
                    durationMinutes = obj.getLong("durationMinutes")
                )
            )
        }
        return list
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().remove(KEY_SESSIONS).apply()
    }
}
