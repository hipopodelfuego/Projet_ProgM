package com.example.projet_mob

import android.app.Activity
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.content.pm.PackageManager
import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class Sixth : Activity() {

    private lateinit var endScreen: android.view.View
    private lateinit var finalScoreText: TextView
    private lateinit var nextButton: android.widget.Button
    private lateinit var scoreText: TextView
    private lateinit var fanImage: ImageView
    private var mediaRecorder: MediaRecorder? = null
    private var maxAmplitude = 0
    private val handler = Handler(Looper.getMainLooper())
    private val recordaudiorequestcode = 101
    private var hasBlown = false
    private var gameEnded = false
    private var finalScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sixth)

        scoreText = findViewById(R.id.scoreText)
        fanImage = findViewById(R.id.fanImage)
        endScreen = findViewById(R.id.endScreen)
        finalScoreText = findViewById(R.id.finalScoreText)
        nextButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("score", finalScore)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        //permission micro
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), recordaudiorequestcode)
        } else {
            startListening()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == recordaudiorequestcode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            Toast.makeText(this, "Permission micro refusée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startListening() {
        try {
            val outputFile = File(cacheDir, "temp_audio.3gp").absolutePath
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                prepare()
                start()
            }

            handler.post(updateRunnable)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur en utilisant le micro : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            val amp = mediaRecorder?.maxAmplitude ?: 0

            if (amp > 1500 && !gameEnded) {
                hasBlown = true
                if (amp > maxAmplitude) maxAmplitude = amp

                val score = ((amp / 32767.0) * 100).toInt().coerceAtMost(100)
                scoreText.text = getString(R.string.score_text, score)
                fanImage.rotation += score / 2f

                handler.postDelayed(this, 100)
            } else if (hasBlown && amp < 1000 && !gameEnded) {
                // Le joueur a soufflé puis s’est arrêté -> fin du jeu
                gameEnded = true
                finalScore = ((maxAmplitude / 32767.0) * 100).toInt().coerceAtMost(100)
                stopListening()

                finalScoreText.text = getString(R.string.score_final_text, finalScore)
                endScreen.visibility = android.view.View.VISIBLE
            } else if (!gameEnded) {
                handler.postDelayed(this, 100)
            }
        }
    }

    private fun stopListening() {
        handler.removeCallbacks(updateRunnable)
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            release()
        }
        mediaRecorder = null
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            release()
        }
        mediaRecorder = null
    }
}
