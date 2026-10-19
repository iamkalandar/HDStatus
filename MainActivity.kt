
package com.hdstatus.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var message: TextView
    private var selectedVideo: Uri? = null
    private var convertedVideo: File? = null
    private var transformer: Transformer? = null
    private var converting = false

    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                selectedVideo = uri
                convertedVideo = null
                message.text =
                    "Video selected. Tap Convert Video to begin."
            } else {
                message.text = "No video selected."
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

        val developer = TextView(this).apply {
            text = "Developed by Kalander"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 20)
        }

        message = TextView(this).apply {
            text = "Convert videos to WhatsApp-friendly MP4"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 20)
        }

        val chooseButton = Button(this).apply {
            text = "Choose Video"
            setOnClickListener {
                if (!converting) {
                    videoPicker.launch("video/*")
                }
            }
        }

        val convertButton = Button(this).apply {
            text = "Convert Video"
            setOnClickListener {
                startConversion()
            }
        }

        val shareButton = Button(this).apply {
            text = "Share Converted Video"
            setOnClickListener {
                shareConvertedVideo()
            }
        }

        val aboutButton = Button(this).apply {
            text = "About / Developer"
            setOnClickListener {
                android.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("About HD Status")
                    .setMessage(
                        "HD Status\n\n" +
                            "Video converter for WhatsApp Status.\n\n" +
                            "Developed by Kalander"
                    )
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        layout.addView(title)
        layout.addView(developer)
        layout.addView(message)
        layout.addView(chooseButton)
        layout.addView(convertButton)
        layout.addView(shareButton)
        layout.addView(aboutButton)

        setContentView(layout)
    }

    @OptIn(UnstableApi::class)
    private fun startConversion() {
        val inputUri = selectedVideo

        if (inputUri == null) {
            message.text = "Please choose a video first."
            return
        }

        if (converting) {
            message.text = "Conversion is already running."
            return
        }

        try {
            val outputDirectory = File(filesDir, "converted")
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }

            val outputFile = File(
                outputDirectory,
                "HDStatus_${System.currentTimeMillis()}.mp4"
            )

            convertedVideo = null
            converting = true
            message.text = "Converting video. Please wait..."

            val listener = object : Transformer.Listener {

                override fun onCompleted(
                    composition: Composition,
                    exportResult: ExportResult
                ) {
                    converting = false
                    transformer = null
                    convertedVideo = outputFile

                    message.text =
                        "Conversion complete!\n" +
                            "MP4 saved inside HD Status.\n" +
                            "Tap Share Converted Video."
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    converting = false
                    transformer = null
                    outputFile.delete()
                    convertedVideo = null

                    message.text =
                        "Conversion failed for this video.\n" +
                            "Try another video format."
                }
            }

            transformer = Transformer.Builder(this)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(listener)
                .build()

            val editedMediaItem = EditedMediaItem.Builder(
                MediaItem.fromUri(inputUri)
            ).build()

            transformer?.start(
                editedMediaItem,
                outputFile.absolutePath
            )
        } catch (exception: Exception) {
            converting = false
            transformer = null
            convertedVideo = null
            message.text =
                "Could not start conversion. Please try another video."
        }
    }

    private fun shareConvertedVideo() {
        val file = convertedVideo

        if (file == null || !file.exists()) {
            message.text = "Convert a video successfully first."
            return
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share video to WhatsApp"
                )
            )
        } catch (exception: Exception) {
            message.text = "Could not share the converted video."
        }
    }

    override fun onDestroy() {
        if (converting) {
            transformer?.cancel()
            transformer = null
            converting = false
        }
        super.onDestroy()
    }
}
