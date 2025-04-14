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

class Third : Activity() {

    private lateinit var questionTextView: TextView
    private lateinit var cocktailImageView: ImageView
    private lateinit var feedbackTextView: TextView
    private lateinit var choice1: Button
    private lateinit var choice2: Button
    private lateinit var choice3: Button
    private lateinit var choice4: Button
    private lateinit var point : TextView
    private lateinit var cocktailsList: MutableList<Cocktail>
    private var score : Int = 0
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var timer: TextView
    private val questionTimeInMillis: Long = 5000

    private lateinit var endScreen: LinearLayout
    private lateinit var finalScoreText: TextView
    private lateinit var endNextButton: Button


    private val cocktails = listOf(
        Cocktail("Mojito", R.drawable.activity3_mojito, "Rhum"),
        Cocktail("Daïquiri", R.drawable.activity3_daiquiri, "Rhum"),
        Cocktail("Punch", R.drawable.activity3_punch, "Rhum"),
        Cocktail("Margarita", R.drawable.activity3_margarita, "Tequila"),
        Cocktail("Orgasme", R.drawable.activity3_orgasme, "Tequila"),
        Cocktail("French 75", R.drawable.activity3_french_75, "Gin"),
        Cocktail("London Mule", R.drawable.activity3_london_mule, "Gin"),
        Cocktail("Dirty martini", R.drawable.activity3_dirty_martini, "Vodka"),
        Cocktail("Sex on the beach", R.drawable.activity3_sex_beach, "Vodka"),
        Cocktail("Bloody Mary", R.drawable.activity3_bloody_mary, "Vodka")
    )

    private var currentCocktailIndex = 0
    private lateinit var correctAnswer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        val cocktails_melange = cocktails.shuffled()

        questionTextView = findViewById(R.id.questionTextView)
        cocktailImageView = findViewById(R.id.cocktailImageView)
        feedbackTextView = findViewById(R.id.feedbackTextView)
        choice1 = findViewById(R.id.choice1)
        choice2 = findViewById(R.id.choice2)
        choice3 = findViewById(R.id.choice3)
        choice4 = findViewById(R.id.choice4)
        point = findViewById(R.id.point)
        timer = findViewById(R.id.timerTextView)

        endScreen = findViewById(R.id.endScreen)
        finalScoreText = findViewById(R.id.finalScoreText)
        endNextButton = findViewById(R.id.nextButton)

        endNextButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        cocktailsList = cocktails_melange.toMutableList()
        loadQuestion()

        val buttons = listOf(choice1, choice2, choice3, choice4)
        for (button in buttons) {
            button.setOnClickListener {
                checkAnswer(button.text.toString())
            }
        }
    }

    private fun loadQuestion() {
        setButtonsEnabled(true)
        point.text = getString(R.string.score_text, score)
        val cocktail = cocktailsList[currentCocktailIndex]
        questionTextView.text = getString(R.string.question_text, cocktail.name)
        cocktailImageView.setImageResource(cocktail.imageRes)
        correctAnswer = cocktail.correctAlcohol

        val options = mutableListOf("Vodka", "Gin", "Rhum", "Tequila").shuffled()
        choice1.text = options[0]
        choice2.text = options[1]
        choice3.text = options[2]
        choice4.text = options[3]

        startTimer()
    }

    private fun checkAnswer(selectedAnswer: String) {
        countDownTimer.cancel()
        if (selectedAnswer == correctAnswer) {
            feedbackTextView.text = getString(R.string.correct_answer)
            score += 1
        } else {
            feedbackTextView.text = getString(R.string.wrong_answer)
        }
        setButtonsEnabled(false)
        currentCocktailIndex++
        if (currentCocktailIndex < cocktailsList.size) {
            feedbackTextView.postDelayed({ loadQuestion() }, 1000)
        } else {
            feedbackTextView.postDelayed({
                showFinalScore()
            }, 1000)
        }
    }
    private fun showFinalScore() {
        questionTextView.text = getString(R.string.quiz_end)
        endScreen.visibility = View.VISIBLE
        finalScoreText.text = getString(R.string.score_final_text, score)
    }
    private fun setButtonsEnabled(enabled: Boolean) {
        choice1.isEnabled = enabled
        choice2.isEnabled = enabled
        choice3.isEnabled = enabled
        choice4.isEnabled = enabled
    }
    private fun startTimer() {
        timer.text = getString(R.string.time_left,5)
        countDownTimer = object : CountDownTimer(questionTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timer.text = getString(R.string.time_left,secondsLeft)
            }

            override fun onFinish() {
                feedbackTextView.text = getString(R.string.time_end)
                setButtonsEnabled(false)
                currentCocktailIndex++
                if (currentCocktailIndex < cocktailsList.size) {
                    feedbackTextView.postDelayed({ loadQuestion() }, 1000)
                } else {
                    feedbackTextView.postDelayed({ showFinalScore() }, 1000)
                }
            }
        }
        countDownTimer.start()
    }

    data class Cocktail(val name: String, val imageRes: Int, val correctAlcohol: String)
}
