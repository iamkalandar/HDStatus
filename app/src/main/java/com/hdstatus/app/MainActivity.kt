
package com.hdstatus.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : Activity() {
    private lateinit var message: TextView
    private var videoUri: Uri? = null

    private val picker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            videoUri = uri
            message.text = if (uri != null) {
                "Video selected. Ready to share."
            } else {
                "No video selected."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "HD Status"
            textSize = 30f
            gravity = Gravity.CENTER
        }

        message = TextView(this).apply {
            text = "Prepare videos for WhatsApp Status"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }

        val choose = Button(this).apply {
            text = "Choose video"
            setOnClickListener { picker.launch("video/*") }
        }

        val share = Button(this).apply {
            text = "Share video"
            setOnClickListener {
                val uri = videoUri
                if (uri != null) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "video/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Share video"))
                } else {
                    message.text = "Choose a video first."
                }
            }
        }

        layout.addView(title)
        layout.addView(message)
        layout.addView(choose)
        layout.addView(share)
        setContentView(layout)
    }
}
