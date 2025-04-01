package com.example.projet_mob

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlin.random.Random


class Fourth : Activity() {

    private lateinit var glassView: GlassView
    private lateinit var startButton: Button
    private lateinit var scoreTextView: TextView

    private var isFilling = false
    private var fillHeight = 0f
    private var targetHeight = 0f
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fourth)

        glassView = findViewById(R.id.glassView)
        startButton = findViewById(R.id.startButton)
        scoreTextView = findViewById(R.id.scoreTextView)

        val glassHeight = 400f
        targetHeight = (Random.nextFloat() * glassHeight * 0.8f)
        glassView.setTargetHeight(targetHeight)

        startButton.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> startFilling()
                android.view.MotionEvent.ACTION_UP -> stopFilling()
            }
            true
        }
    }

    private fun startFilling() {
        isFilling = true
        handler.post(fillRunnable)
    }

    private fun stopFilling() {
        isFilling = false
        startButton.isEnabled = false // Désactiver le bouton après relâchement
        val score = 100 - Math.abs(targetHeight - fillHeight).toInt()
        scoreTextView.text = "Score : $score"
    }

    private val fillRunnable = object : Runnable {
        override fun run() {
            if (isFilling) {
                val maxFillHeight = 400f

                if (fillHeight < maxFillHeight) {
                    fillHeight += 5f
                    glassView.setFillHeight(fillHeight)
                }

                handler.postDelayed(this, 100)
            }
        }
    }

}
