package com.boxmonitor.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boxmonitor.R
import com.boxmonitor.data.AppPreferences
import com.boxmonitor.data.ScreenshotManager
import com.boxmonitor.data.UptimeManager
import com.boxmonitor.service.MonitoringService

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_MEDIA_PROJECTION = 1001
    }

    private lateinit var switchMonitoring: Switch
    private lateinit var tvStatus: TextView
    private lateinit var tvScreenshotCount: TextView
    private lateinit var tvTotalSize: TextView
    private lateinit var btnViewScreenshots: Button
    private lateinit var btnDeleteScreenshots: Button
    private lateinit var rvUptime: RecyclerView
    private lateinit var tvCurrentUptime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun initViews() {
        switchMonitoring = findViewById(R.id.switchMonitoring)
        tvStatus = findViewById(R.id.tvStatus)
        tvScreenshotCount = findViewById(R.id.tvScreenshotCount)
        tvTotalSize = findViewById(R.id.tvTotalSize)
        btnViewScreenshots = findViewById(R.id.btnViewScreenshots)
        btnDeleteScreenshots = findViewById(R.id.btnDeleteScreenshots)
        rvUptime = findViewById(R.id.rvUptime)
        tvCurrentUptime = findViewById(R.id.tvCurrentUptime)

        rvUptime.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setMonitoringEnabled(this, isChecked)
            if (isChecked) {
                requestMediaProjection()
            } else {
                stopService(Intent(this, MonitoringService::class.java))
                updateStatus(false)
            }
        }

        btnViewScreenshots.setOnClickListener {
            startActivity(Intent(this, ScreenshotListActivity::class.java))
        }

        btnDeleteScreenshots.setOnClickListener {
            confirmDeleteScreenshots()
        }
    }

    private fun requestMediaProjection() {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val serviceIntent = Intent(this, MonitoringService::class.java).apply {
                    action = MonitoringService.ACTION_START
                    putExtra(MonitoringService.EXTRA_RESULT_CODE, resultCode)
                    putExtra(MonitoringService.EXTRA_RESULT_DATA, data)
                }
                startForegroundService(serviceIntent)
                updateStatus(true)
            } else {
                // Permission refusée
                switchMonitoring.isChecked = false
                AppPreferences.setMonitoringEnabled(this, false)
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        val enabled = AppPreferences.isMonitoringEnabled(this)
        switchMonitoring.isChecked = enabled && MonitoringService.isRunning
        updateStatus(MonitoringService.isRunning)

        val count = ScreenshotManager.getScreenshotCount(this)
        val sizeMb = ScreenshotManager.getTotalSizeMb(this)
        tvScreenshotCount.text = "$count captures"
        tvTotalSize.text = String.format("%.1f MB", sizeMb)

        // Uptime courant
        val startTime = AppPreferences.getStartTime(this)
        if (startTime > 0 && MonitoringService.isRunning) {
            val elapsed = System.currentTimeMillis() - startTime
            val hours = elapsed / 3600000
            val minutes = (elapsed % 3600000) / 60000
            tvCurrentUptime.text = "Démarré à ${AppPreferences.formatTime(startTime)} (${hours}h ${minutes}min)"
        } else {
            tvCurrentUptime.text = "Service inactif"
        }

        // Liste uptime
        val sessions = UptimeManager.getSessions(this).take(10)
        rvUptime.adapter = UptimeAdapter(sessions)
    }

    private fun updateStatus(running: Boolean) {
        if (running) {
            tvStatus.text = "● ACTIF"
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            tvStatus.text = "○ INACTIF"
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    private fun confirmDeleteScreenshots() {
        AlertDialog.Builder(this)
            .setTitle("Supprimer les captures")
            .setMessage("Voulez-vous supprimer TOUTES les captures d'écran ?")
            .setPositiveButton("Supprimer") { _, _ ->
                val count = ScreenshotManager.deleteAllScreenshots(this)
                Toast.makeText(this, "$count fichiers supprimés", Toast.LENGTH_SHORT).show()
                updateUI()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}
