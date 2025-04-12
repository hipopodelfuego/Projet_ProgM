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
    private lateinit var nextButton: Button
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var timer: TextView
    private val questionTimeInMillis: Long = 5000

    private lateinit var endScreen: LinearLayout
    private lateinit var finalScoreText: TextView
    private lateinit var endNextButton: Button


    private val cocktails = listOf(
        Cocktail("Mojito", R.drawable.mojito, "Rhum"),
        Cocktail("Daïquiri", R.drawable.daiquiri, "Rhum"),
        Cocktail("Punch", R.drawable.punch, "Rhum"),
        Cocktail("Margarita", R.drawable.margarita, "Tequila"),
        Cocktail("Orgasme", R.drawable.orgasme, "Tequila"),
        Cocktail("French 75", R.drawable.french_75, "Gin"),
        Cocktail("London Mule", R.drawable.london_mule, "Gin"),
        Cocktail("Dirty martini", R.drawable.dirty_martini, "Vodka"),
        Cocktail("Sex on the beach", R.drawable.sex_beach, "Vodka"),
        Cocktail("Bloody Mary", R.drawable.bloody_mary, "Vodka")
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
            setResult(Activity.RESULT_OK, resultIntent)
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
        point.text = "Score : ${score}"
        val cocktail = cocktailsList[currentCocktailIndex]
        questionTextView.text = "Quel est l'alcool dans ${cocktail.name} ?"
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
            feedbackTextView.text = "Bonne réponse ! 🎉"
            Toast.makeText(this, "Bonne réponse !", Toast.LENGTH_SHORT).show()
            score += 1
        } else {
            feedbackTextView.text = "Mauvaise réponse... 😞 C'était $correctAnswer"
            Toast.makeText(this, "Mauvaise réponse !", Toast.LENGTH_SHORT).show()
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
        questionTextView.text = "Quiz terminé 🎉"
        endScreen.visibility = View.VISIBLE
        finalScoreText.text = "Votre score final est : $score / ${cocktailsList.size}"
    }
    private fun setButtonsEnabled(enabled: Boolean) {
        choice1.isEnabled = enabled
        choice2.isEnabled = enabled
        choice3.isEnabled = enabled
        choice4.isEnabled = enabled
    }
    private fun startTimer() {
        timer.text = "Temps restant : 5s"
        countDownTimer = object : CountDownTimer(questionTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timer.text = "Temps restant : $secondsLeft s"
            }

            override fun onFinish() {
                feedbackTextView.text = "Temps écoulé ! ⏰ C'était $correctAnswer"
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
