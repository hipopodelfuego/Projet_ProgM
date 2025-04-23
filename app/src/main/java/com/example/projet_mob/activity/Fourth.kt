package com.example.projet_mob.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.projet_mob.R
import kotlin.math.abs
import kotlin.random.Random

class Fourth : Activity() {

    private lateinit var endScreen: View
    private lateinit var finalScoreText: TextView
    private lateinit var nextButton: Button
    private lateinit var glassView: GlassView
    private lateinit var startButton: Button
    private lateinit var scoreTextView: TextView
    private var score: Int = 0
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
        scoreTextView.text = getString(R.string.score_text, 0)
        endScreen = findViewById(R.id.endScreen)
        finalScoreText = findViewById(R.id.finalScoreText)
        nextButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            setResult(RESULT_OK, resultIntent)
            finish()
        }


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
        startButton.isEnabled = false

        score = 100 - abs(targetHeight - fillHeight).toInt()
        scoreTextView.text = getString(R.string.score_text, score)

        finalScoreText.text = getString(R.string.score_final_text, score)
        endScreen.visibility = View.VISIBLE
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
