package com.boxmonitor.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boxmonitor.R
import com.boxmonitor.data.ScreenshotFile
import com.boxmonitor.data.ScreenshotManager

class ScreenshotListActivity : AppCompatActivity() {

    private lateinit var rvScreenshots: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot_list)

        title = "Captures d'écran"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvScreenshots = findViewById(R.id.rvScreenshots)
        tvEmpty = findViewById(R.id.tvEmpty)

        rvScreenshots.layoutManager = GridLayoutManager(this, 2)
        loadScreenshots()
    }

    private fun loadScreenshots() {
        val screenshots = ScreenshotManager.getScreenshots(this)
        if (screenshots.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvScreenshots.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvScreenshots.visibility = View.VISIBLE
            rvScreenshots.adapter = ScreenshotAdapter(screenshots)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class ScreenshotAdapter(private val items: List<ScreenshotFile>) :
    RecyclerView.Adapter<ScreenshotAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivScreenshot)
        val tvDate: TextView = view.findViewById(R.id.tvScreenshotDate)
        val tvTime: TextView = view.findViewById(R.id.tvScreenshotTime)
        val tvSize: TextView = view.findViewById(R.id.tvScreenshotSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_screenshot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = item.dateFormatted
        holder.tvTime.text = item.timeFormatted
        holder.tvSize.text = "${item.sizeKb} KB"

        // Charger l'image en thumbnail
        try {
            val opts = BitmapFactory.Options().apply {
                inSampleSize = 4 // réduire la taille pour les thumbnails
            }
            val bmp = BitmapFactory.decodeFile(item.filePath, opts)
            holder.imageView.setImageBitmap(bmp)
        } catch (e: Exception) {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun getItemCount() = items.size
}
