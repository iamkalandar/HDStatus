package com.hdstatus.app

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.common.MediaItem
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File

/**
 * HD Status - first MVP.
 * Prepares a selected video as an MP4 in a 1080 x 1920 portrait canvas.
 * The source video is fit within the canvas to avoid cropping; unused space
 * is filled by the presentation effect as supported by Media3.
 */
class MainActivity : ComponentActivity() {
    private lateinit var status: TextView
    private lateinit var chooseButton: Button
    private lateinit var convertButton: Button
    private lateinit var shareButton: Button
    private lateinit var progress: ProgressBar
    private var selectedVideo: android.net.Uri? = null
    private var outputFile: File? = null
    private var transformer: Transformer? = null

    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedVideo = uri
            outputFile = null
            status.text = "Video selected. Convert it to a 1080p Status video."
            convertButton.isEnabled = true
            shareButton.isEnabled = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(0xFFF7F8FA.toInt())
        }
        val title = TextView(this).apply {
            text = "HD Status"
            textSize = 30f
            setTextColor(0xFF172033.toInt())
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "WhatsApp Status • 1080p Full HD"
            textSize = 16f
            setTextColor(0xFF606A7B.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 20)
        }
        val info = TextView(this).apply {
            text = "Prepare a video for vertical Status sharing. The first version exports MP4 in a 1080 × 1920 portrait frame."
            textSize = 14f
            setTextColor(0xFF606A7B.toInt())
            gravity = Gravity.CENTER
            setPadding(8, 0, 8, 18)
        }
        status = TextView(this).apply {
            text = "Choose a video from your phone to get started."
            textSize = 16f
            setTextColor(0xFF263247.toInt())
            gravity = Gravity.CENTER
            setPadding(12, 18, 12, 18)
        }
        chooseButton = makeButton("Choose video") { picker.launch("video/*") }
        convertButton = makeButton("Prepare 1080p Status video") { convertVideo() }.apply { isEnabled = false }
        shareButton = makeButton("Save and share video") { shareOutput() }.apply { isEnabled = false }
        progress = ProgressBar(this).apply { visibility = ProgressBar.GONE }

        root.addView(title, matchWrap())
        root.addView(subtitle, matchWrap())
        root.addView(info, matchWrap())
        root.addView(chooseButton, fullButton())
        root.addView(convertButton, fullButton())
        root.addView(shareButton, fullButton())
        root.addView(progress, LinearLayout.LayoutParams(48, 48).apply { topMargin = 20 })
        root.addView(status, matchWrap())
        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 16f
        setOnClickListener { action() }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun fullButton() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = 10 }

    private fun convertVideo() {
        val input = selectedVideo ?: return
        val out = File(cacheDir, "HDStatus_${System.currentTimeMillis()}.mp4")
        outputFile = out
        convertButton.isEnabled = false
        chooseButton.isEnabled = false
        shareButton.isEnabled = false
        progress.visibility = ProgressBar.VISIBLE
        status.text = "Preparing 1080p video… keep the app open."

        // A portrait canvas is suitable for Status. FIT preserves the whole
        // source image instead of cutting off the sides/top of the video.
        val presentation = Presentation.createForWidthAndHeight(
            1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT
        )
        val effects = Effects(emptyList(), listOf(presentation))
        val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(input))
            .setEffects(effects)
            .build()

        transformer = Transformer.Builder(this)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    progress.visibility = ProgressBar.GONE
                    chooseButton.isEnabled = true
                    convertButton.isEnabled = true
                    shareButton.isEnabled = out.exists() && out.length() > 0
                    status.text = "Ready! Save or share your 1080p MP4 video."
                    Toast.makeText(this@MainActivity, "Status video prepared", Toast.LENGTH_SHORT).show()
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    progress.visibility = ProgressBar.GONE
                    chooseButton.isEnabled = true
                    convertButton.isEnabled = true
                    shareButton.isEnabled = false
                    out.delete()
                    status.text = "Conversion failed: ${exportException.message ?: "unsupported video"}"
                    Toast.makeText(this@MainActivity, "Could not convert this video", Toast.LENGTH_LONG).show()
                }
            })
            .build()

        try {
            transformer?.start(editedItem, out.absolutePath)
        } catch (e: Exception) {
            progress.visibility = ProgressBar.GONE
            chooseButton.isEnabled = true
            convertButton.isEnabled = true
            shareButton.isEnabled = false
            out.delete()
            status.text = "Could not start conversion: ${e.message ?: "unknown error"}"
        }
    }

    private fun shareOutput() {
        val file = outputFile
        if (file == null || !file.exists() || file.length() == 0L) {
            Toast.makeText(this, "Prepare a video first.", Toast.LENGTH_SHORT).show()
            return
        }

        val values = ContentValues().apply {
            put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/HD Status")
        }
        val savedUri = contentResolver.insert(
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
        )
        if (savedUri == null) {
            Toast.makeText(this, "Could not save video to Movies/HD Status.", Toast.LENGTH_LONG).show()
            return
        }

        try {
            file.inputStream().use { input ->
                val output = contentResolver.openOutputStream(savedUri)
                    ?: throw IllegalStateException("Could not open destination file")
                output.use { input.copyTo(it) }
            }
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, savedUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(send, "Share HD Status video"))
            status.text = "Saved to Movies/HD Status. Choose WhatsApp to share it."
        } catch (e: Exception) {
            contentResolver.delete(savedUri, null, null)
            status.text = "Could not save/share video: ${e.message ?: "unknown error"}"
        }
    }

    override fun onDestroy() {
        // Stop an in-progress export when the activity is destroyed.
        transformer?.cancel()
        transformer = null
        super.onDestroy()
    }
}
