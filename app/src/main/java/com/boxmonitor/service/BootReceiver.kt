package com.boxmonitor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.boxmonitor.data.AppPreferences
import com.boxmonitor.data.UptimeManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            if (AppPreferences.isMonitoringEnabled(context)) {
                UptimeManager.recordStart(context)
            }
        }
    }
}
