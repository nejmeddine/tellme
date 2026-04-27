package com.boxmonitor.data

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ScreenshotManager {

    private const val FOLDER_NAME = "BoxMonitorScreenshots"

    fun getScreenshotFolder(context: Context): File {
        val folder = File(context.getExternalFilesDir(null), FOLDER_NAME)
        if (!folder.exists()) folder.mkdirs()
        return folder
    }

    fun saveScreenshot(context: Context, bitmap: Bitmap): String? {
        return try {
            val folder = getScreenshotFolder(context)
            val timestamp = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "screenshot_${sdf.format(Date(timestamp))}.jpg"
            val file = File(folder, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getScreenshots(context: Context): List<ScreenshotFile> {
        val folder = getScreenshotFolder(context)
        if (!folder.exists()) return emptyList()

        return folder.listFiles { f -> f.extension == "jpg" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                ScreenshotFile(
                    filePath = file.absolutePath,
                    timestamp = file.lastModified(),
                    dateFormatted = AppPreferences.formatDate(file.lastModified()),
                    timeFormatted = AppPreferences.formatTime(file.lastModified()),
                    sizeKb = file.length() / 1024
                )
            } ?: emptyList()
    }

    fun deleteAllScreenshots(context: Context): Int {
        val folder = getScreenshotFolder(context)
        if (!folder.exists()) return 0
        var count = 0
        folder.listFiles()?.forEach { file ->
            if (file.delete()) count++
        }
        return count
    }

    fun getScreenshotCount(context: Context): Int {
        val folder = getScreenshotFolder(context)
        return folder.listFiles()?.size ?: 0
    }

    fun getTotalSizeMb(context: Context): Float {
        val folder = getScreenshotFolder(context)
        val totalBytes = folder.listFiles()?.sumOf { it.length() } ?: 0L
        return totalBytes / (1024f * 1024f)
    }
}
