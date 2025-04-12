package com.example.projet_mob

import android.app.Activity
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class First : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var glassImageView: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var finalScoreText: TextView
    private lateinit var nextButton: Button
    private lateinit var endScreen: View
    private lateinit var gameLayout: FrameLayout
    private val activeDrops = mutableListOf<ImageView>()
    private val maxDrops = 5

    private var glassX = 0f
    private var screenWidth = 0
    private var score = 0
    private val dropSize = 80
    private lateinit var timerTextView: TextView
    private var gameRunning = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        glassImageView = findViewById(R.id.glassImageView)
        scoreTextView = findViewById(R.id.scoreTextView)
        finalScoreText = findViewById(R.id.finalScoreText)
        nextButton = findViewById(R.id.nextButton)
        endScreen = findViewById(R.id.endScreen)
        gameLayout = findViewById(R.id.gameLayout)
        timerTextView = findViewById(R.id.timerTextView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        nextButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val xTilt = -event.values[0]
            glassX += xTilt * 10
            val maxX = screenWidth - glassImageView.width
            glassX = max(0f, min(glassX, maxX.toFloat()))
            glassImageView.x = glassX
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startDropLoop() {
        gameRunning = true
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "Temps restant : $secondsLeft s"
                spawnDrop()
            }

            override fun onFinish() {
                gameRunning = false
                timerTextView.text = "Temps écoulé !"
                showEndScreen()
            }
        }.start()
    }


    private fun spawnDrop() {
        if (!gameRunning || activeDrops.size >= maxDrops) return

        val isAlcohol = Random.nextBoolean()
        val drop = ImageView(this)
        val drawable = if (isAlcohol) R.drawable.goutte_alcool else R.drawable.goutte_eau
        drop.setImageResource(drawable)

        val params = FrameLayout.LayoutParams(dropSize, dropSize)
        drop.layoutParams = params

        val startX = Random.nextInt(screenWidth - dropSize)
        drop.x = startX.toFloat()
        drop.y = 0f

        gameLayout.addView(drop)
        activeDrops.add(drop)

        val dropSpeed = 15

        drop.post(object : Runnable {
            override fun run() {
                if (!gameRunning || !drop.isAttachedToWindow) {
                    if (drop.parent != null) gameLayout.removeView(drop)
                    activeDrops.remove(drop)
                    return
                }

                drop.y += dropSpeed
                if (drop.y + dropSize >= glassImageView.y &&
                    drop.x + dropSize >= glassImageView.x &&
                    drop.x <= glassImageView.x + glassImageView.width
                ) {
                    gameLayout.removeView(drop)
                    activeDrops.remove(drop)
                    score += if (isAlcohol) 1 else -1
                    scoreTextView.text = "Score : $score"
                } else if (drop.y > gameLayout.height) {
                    gameLayout.removeView(drop)
                    activeDrops.remove(drop)
                } else {
                    drop.postDelayed(this, 16)
                }
            }
        })
    }

    private fun showEndScreen() {
        gameRunning = false

        for (drop in activeDrops) {
            if (drop.parent != null) {
                gameLayout.removeView(drop)
            }
        }
        activeDrops.clear()

        endScreen.visibility = View.VISIBLE
        finalScoreText.text = "Score final : $score"
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && screenWidth == 0) {
            screenWidth = gameLayout.width
            glassX = glassImageView.x
            startDropLoop()
        }
    }

}
