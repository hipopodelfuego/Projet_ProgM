package com.example.projet_mob

import android.os.Vibrator
import android.os.VibrationEffect
import android.content.Context
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlin.random.Random

class Second : Activity(), GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var bottleImageView: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var nextButton: Button

    private lateinit var endScreen: View
    private lateinit var finalScoreText: TextView
    private lateinit var endNextButton: Button

    private var score = 0
    private var isGameRunning = false
    private var currentBottleType = "" // "bouchon" ou "vis"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        bottleImageView = findViewById(R.id.bottleImageView)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)
        nextButton = findViewById(R.id.btnNext)
        nextButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // retourne à SoloGame → onActivityResult → next
        }
        endScreen = findViewById(R.id.endScreen)
        finalScoreText = findViewById(R.id.finalScoreText)
        endNextButton = findViewById(R.id.nextButton)

        endScreen.visibility = View.GONE

        endNextButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        gestureDetector = GestureDetector(this, this)

        startGame()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    private fun animateBottle(isBouchon: Boolean) {
        val moveAnimation: ObjectAnimator
        val fadeOut = ObjectAnimator.ofFloat(bottleImageView, "alpha", 0f)
        fadeOut.duration = 300

        if (isBouchon) {
            // Animation vers le haut (décapsuler)
            moveAnimation = ObjectAnimator.ofFloat(bottleImageView, "translationY", -300f)
        } else {
            // Animation vers la droite (dévisser)
            moveAnimation = ObjectAnimator.ofFloat(bottleImageView, "translationX", 300f)
        }

        moveAnimation.duration = 500
        moveAnimation.interpolator = DecelerateInterpolator()

        // Jouer les animations ensemble
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveAnimation, fadeOut)
        animatorSet.start()

        // Réinitialisation après animation
        animatorSet.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}

            override fun onAnimationEnd(animation: android.animation.Animator) {
                bottleImageView.translationY = 0f
                bottleImageView.translationX = 0f
                bottleImageView.alpha = 1f
                newBottle()
            }

            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }


    // Fonction pour vibrer lors d'une bonne action
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(100)
        }
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
                animateBottle(true)
                vibrate()

            } else if (currentBottleType == "vis" && deltaX > 100) {
                // Swipe gauche-droite pour dévisser
                score++
                animateBottle(false)
                vibrate()
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
                timerTextView.text = "Temps écoulé !"
                endScreen.visibility = View.VISIBLE
                finalScoreText.text = "Score final : $score"
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
