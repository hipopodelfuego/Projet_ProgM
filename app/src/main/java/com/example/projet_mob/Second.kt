package com.example.projet_mob

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import kotlin.random.Random

class Second : Activity(), GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var bottleImageView: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView

    private var score = 0
    private var isGameRunning = false
    private var currentBottleType = "" // "bouchon" ou "vis"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        bottleImageView = findViewById(R.id.bottleImageView)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)

        gestureDetector = GestureDetector(this, this)

        startGame()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (!isGameRunning) return false

        if (e1 != null) {
            val deltaX = e2.x - e1.x
            val deltaY = e2.y - e1.y

            if (currentBottleType == "bouchon" && deltaY < -100) {
                // Swipe vers le haut pour déboucher la bouteille
                score++
                newBottle()
            } else if (currentBottleType == "vis" && deltaX > 100) {
                // Swipe gauche-droite pour dévisser
                score++
                newBottle()
            }
        }
        return true
    }

    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false
    override fun onLongPress(e: MotionEvent) {}

    private fun startGame() {
        score = 0
        isGameRunning = true
        timerTextView.text = "Temps restant : 30s"

        newBottle()

        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Temps restant : ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                isGameRunning = false
                timerTextView.text = "⏳ Temps écoulé ! Score final : $score"
            }
        }.start()
    }

    private fun newBottle() {
        // Choix aléatoire de la bouteille
        currentBottleType = if (Random.nextBoolean()) "bouchon" else "vis"

        if (currentBottleType == "bouchon") {
            bottleImageView.setImageResource(R.drawable.bouteille_bouchon) // Image de bouteille à débouchon
        } else {
            bottleImageView.setImageResource(R.drawable.bouteille_vis) // Image de bouteille à vis
        }

        scoreTextView.text = "Score : $score"
    }
}
