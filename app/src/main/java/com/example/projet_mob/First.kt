package com.example.projet_mob

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class First : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var gameOver = false
    private var score = 0

    private lateinit var verreImageView: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        verreImageView = findViewById(R.id.verreImageView)
        scoreTextView = findViewById(R.id.scoreTextView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        handler = Handler(Looper.getMainLooper())
        startScoring()
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
    }

    var lastTime = System.currentTimeMillis()
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && !gameOver) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime < 100) return

            lastTime = currentTime

            val rotationX = event.values[0]
            val rotationY = event.values[1]
            val rotationZ = event.values[2]

            val seuilMax = 1.5f
            val seuilMin = 0.05f

            val mouvementTotal = Math.abs(rotationX) + Math.abs(rotationY) + Math.abs(rotationZ)

            when {
                mouvementTotal > seuilMax -> loseGame("T'as renversé ton verre ! Trop violent 🍻")
                mouvementTotal < seuilMin -> loseGame("Bouge ton verre ! Tu dors ou quoi ? 😴")
                else -> {
                    // Animation optionnelle
                    verreImageView.rotation += mouvementTotal * 2
                }
            }
        }
    }

    private fun startScoring() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!gameOver) {
                    score++
                    scoreTextView.text = "Score : $score sec"
                    handler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    private fun loseGame(message: String) {
        gameOver = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        handler.removeCallbacksAndMessages(null)
        scoreTextView.text = "Score final : $score sec"
        verreImageView.setImageResource(R.drawable.verre_renverse) // image du verre renversé
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        gyroscope?.also { gyro ->
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
