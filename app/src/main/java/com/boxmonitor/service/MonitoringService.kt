package com.boxmonitor.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.boxmonitor.R
import com.boxmonitor.data.AppPreferences
import com.boxmonitor.data.ScreenshotManager
import com.boxmonitor.data.UptimeManager
import com.boxmonitor.ui.LoginActivity
import kotlinx.coroutines.*
import java.nio.ByteBuffer

class MonitoringService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
        const val CHANNEL_ID = "BoxMonitorChannel"
        const val NOTIFICATION_ID = 1001

        var isRunning = false
        var mediaProjectionResultCode = 0
        var mediaProjectionData: Intent? = null
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var screenshotJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var screenWidth = 1920
    private var screenHeight = 1080
    private var screenDensity = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        getScreenMetrics()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val data = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

                if (resultCode == Activity.RESULT_OK && data != null) {
                    startForeground(NOTIFICATION_ID, buildNotification())
                    startProjection(resultCode, data)
                    UptimeManager.recordStart(this)
                    isRunning = true
                    startScreenshotLoop()
                }
            }
            ACTION_STOP -> {
                stopMonitoring()
            }
        }
        return START_STICKY
    }

    private fun getScreenMetrics() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(metrics)
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
        }
        screenDensity = resources.displayMetrics.densityDpi
    }

    private fun startProjection(resultCode: Int, data: Intent) {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "BoxMonitor",
            screenWidth, screenHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
    }

    private fun startScreenshotLoop() {
        screenshotJob = serviceScope.launch {
            while (isActive && isRunning) {
                delay(60_000L) // toutes les 60 secondes
                if (AppPreferences.isMonitoringEnabled(this@MonitoringService)) {
                    captureScreenshot()
                }
            }
        }
    }

    private fun captureScreenshot() {
        try {
            val image = imageReader?.acquireLatestImage() ?: return
            val planes = image.planes
            val buffer: ByteBuffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * screenWidth

            val bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride,
                screenHeight,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            image.close()

            val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
            bitmap.recycle()

            ScreenshotManager.saveScreenshot(this, croppedBitmap)
            croppedBitmap.recycle()

            updateNotification()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopMonitoring() {
        isRunning = false
        screenshotJob?.cancel()
        UptimeManager.recordStop(this)
        virtualDisplay?.release()
        mediaProjection?.stop()
        imageReader?.close()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Box Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Service de surveillance actif"
            setShowBadge(false)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, LoginActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val count = ScreenshotManager.getScreenshotCount(this)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Box Monitor")
            .setContentText("Surveillance active • $count captures")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (isRunning) {
            UptimeManager.recordStop(this)
            isRunning = false
        }
    }
}
