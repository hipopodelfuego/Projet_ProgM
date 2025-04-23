package com.example.projet_mob.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.os.CountDownTimer
import android.view.View
import android.widget.LinearLayout
import com.example.projet_mob.R

class Fifth : Activity() {

    private lateinit var questionTextView: TextView
    private lateinit var feedbackTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var option1: ImageView
    private lateinit var option2: ImageView
    private lateinit var option3: ImageView
    private lateinit var option4: ImageView
    private var correctImageRes: Int = 0
    private var score: Int = 0
    private var currentQuestionIndex = 0
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var timer: TextView
    private val questionTimeInMillis: Long = 5000

    private lateinit var finalScoreText: TextView
    private lateinit var endNextButton: Button


    data class Question(
        val text: String,
        val correctImageRes: Int
    )

    private val questions = listOf(
        Question("Trouve le bar : La Maison", R.drawable.activity5_maison),
        Question("Trouve le bar : l'Annexe", R.drawable.activity5_annexe),
        Question("Trouve le bar : le Petit Vélo", R.drawable.activity5_leptitvelo),
        Question("Trouve le bar : le Delirium",  R.drawable.activity5_deli),
        Question("Trouve le bar : le Zing",  R.drawable.activity5_lezing),
        Question("Trouve le bar : le Baratin",  R.drawable.activity5_baratin),
        Question("Trouve le bar : l'Uzine'", R.drawable.activity5_uzine),
        Question("Trouve le bar : Tiffany's Pub", R.drawable.activity5_typh),
        Question("Trouve le bar : la cave à flo",  R.drawable.activity5_cave),
        Question("Trouve le bar : Le V&B", R.drawable.activity5_vb),
    )

    private val allBarsImages = listOf(
        R.drawable.activity5_maison, R.drawable.activity5_annexe, R.drawable.activity5_leptitvelo,
        R.drawable.activity5_vb, R.drawable.activity5_deli, R.drawable.activity5_typh,
        R.drawable.activity5_lezing, R.drawable.activity5_baratin, R.drawable.activity5_uzine,
        R.drawable.activity5_cave
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fifth)

        questionTextView = findViewById(R.id.questionTextView)
        feedbackTextView = findViewById(R.id.feedbackTextView)
        scoreTextView = findViewById(R.id.scoreTextView)

        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)

        finalScoreText = findViewById(R.id.finalScoreText)
        endNextButton = findViewById(R.id.nextButton)

        timer = findViewById(R.id.timerTextView)

        endNextButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        val options = listOf(option1, option2, option3, option4)

        options.forEach { imageView ->
            imageView.setOnClickListener {
                checkAnswer((it as ImageView).tag as Int)
            }
        }

        loadQuestion()
    }

    private fun loadQuestion() {
        setButtonsEnabled(true)
        val question = questions[currentQuestionIndex]
        questionTextView.text = question.text
        correctImageRes = question.correctImageRes

        //on prend 3 images en plus que l/'on ajoute
        val wrongOptions = (allBarsImages - correctImageRes).shuffled().take(3)
        val allOptions = (wrongOptions + correctImageRes).shuffled()

        val imageViews = listOf(option1, option2, option3, option4)
        for (i in imageViews.indices) {
            imageViews[i].setImageResource(allOptions[i])
            imageViews[i].tag = allOptions[i]
        }

        scoreTextView.text = getString(R.string.score_text, score)
        feedbackTextView.text = ""

        startTimer()
    }

    private fun checkAnswer(selectedImageRes: Int) {
        countDownTimer.cancel()
        setButtonsEnabled(false)
        if (selectedImageRes == correctImageRes) {
            feedbackTextView.text = getString(R.string.correct_answer)
            Toast.makeText(this, "Correct !", Toast.LENGTH_SHORT).show()
            score++
        } else {
            feedbackTextView.text = getString(R.string.wrong_answer)
            Toast.makeText(this, "Incorrect", Toast.LENGTH_SHORT).show()
        }

        currentQuestionIndex++
        if (currentQuestionIndex < questions.size) {
            feedbackTextView.postDelayed({ loadQuestion() }, 2000)
        } else {
            feedbackTextView.postDelayed({
                showEndScreen()
            }, 2000)
        }
    }
    private fun showEndScreen() {
        questionTextView.text = getString(R.string.quiz_end)
        findViewById<LinearLayout>(R.id.endScreen).visibility = LinearLayout.VISIBLE
        finalScoreText.text = getString(R.string.score_final_text, score)
        scoreTextView.visibility= View.GONE
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        option1.isEnabled = enabled
        option2.isEnabled = enabled
        option3.isEnabled = enabled
        option4.isEnabled = enabled
    }

    private fun startTimer() {
        timer.text = getString(R.string.time_left, 5)


        countDownTimer = object : CountDownTimer(questionTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timer.text = getString(R.string.time_left,secondsLeft)
            }

            override fun onFinish() {
                feedbackTextView.text = getString(R.string.time_end)
                setButtonsEnabled(false)
                currentQuestionIndex++
                if (currentQuestionIndex < questions.size) {
                    feedbackTextView.postDelayed({ loadQuestion() }, 1000)
                } else {
                    feedbackTextView.postDelayed({ showEndScreen() }, 1000)
                }
            }
        }
        countDownTimer.start()
    }
}
