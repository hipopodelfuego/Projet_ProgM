package com.example.projet_mob

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
        val options: List<Int>,
        val correctImageRes: Int
    )

    private val questions = listOf(
        Question("Trouve le bar : la maison", listOf(R.drawable.maison, R.drawable.annexe, R.drawable.leptitvelo, R.drawable.vb), R.drawable.maison),
        Question("Trouve le bar : l'annexe", listOf(R.drawable.annexe, R.drawable.baratin, R.drawable.lezing, R.drawable.uzine), R.drawable.annexe),
        Question("Trouve le bar : le petit velo", listOf(R.drawable.leptitvelo, R.drawable.lezing, R.drawable.cave, R.drawable.maison), R.drawable.leptitvelo),
        Question("Trouve le bar : le delirium", listOf(R.drawable.deli, R.drawable.typh, R.drawable.maison, R.drawable.baratin), R.drawable.deli),
        Question("Trouve le bar : le zing", listOf(R.drawable.lezing, R.drawable.uzine, R.drawable.vb, R.drawable.annexe), R.drawable.lezing),
        Question("Trouve le bar : le baratin", listOf(R.drawable.baratin, R.drawable.typh, R.drawable.leptitvelo, R.drawable.cave), R.drawable.baratin),
        Question("Trouve le bar : l'uzine'", listOf(R.drawable.uzine, R.drawable.leptitvelo, R.drawable.baratin, R.drawable.annexe), R.drawable.uzine),
        Question("Trouve le bar : Tiffany's Pub", listOf(R.drawable.typh, R.drawable.deli, R.drawable.maison, R.drawable.cave), R.drawable.typh),
        Question("Trouve le bar : la cave à flo", listOf(R.drawable.cave, R.drawable.vb, R.drawable.deli, R.drawable.uzine), R.drawable.cave),
        Question("Trouve le bar : Le V&B", listOf(R.drawable.vb, R.drawable.uzine, R.drawable.deli, R.drawable.baratin), R.drawable.vb),
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
            setResult(Activity.RESULT_OK, resultIntent)
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

        val shuffledOptions = question.options.shuffled()

        option1.setImageResource(shuffledOptions[0])
        option1.tag = shuffledOptions[0]

        option2.setImageResource(shuffledOptions[1])
        option2.tag = shuffledOptions[1]

        option3.setImageResource(shuffledOptions[2])
        option3.tag = shuffledOptions[2]

        option4.setImageResource(shuffledOptions[3])
        option4.tag = shuffledOptions[3]

        scoreTextView.text = "Score : $score"
        feedbackTextView.text = ""

        startTimer()
    }

    private fun checkAnswer(selectedImageRes: Int) {
        countDownTimer.cancel()
        setButtonsEnabled(false)
        if (selectedImageRes == correctImageRes) {
            feedbackTextView.text = "Bonne réponse 🎉"
            Toast.makeText(this, "Correct !", Toast.LENGTH_SHORT).show()
            score++
        } else {
            feedbackTextView.text = "Mauvaise réponse 😞"
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
        questionTextView.text = "🎉 Quiz terminé !"
        findViewById<LinearLayout>(R.id.endScreen).visibility = LinearLayout.VISIBLE
        finalScoreText.text = "Votre score final est : $score"
        scoreTextView.visibility= View.GONE
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        option1.isEnabled = enabled
        option2.isEnabled = enabled
        option3.isEnabled = enabled
        option4.isEnabled = enabled
    }

    private fun startTimer() {
        timer.text = "Temps restant : 5s"

        countDownTimer = object : CountDownTimer(questionTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timer.text = "Temps restant : $secondsLeft s"
            }

            override fun onFinish() {
                feedbackTextView.text = "Temps écoulé ! ⏰"
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
