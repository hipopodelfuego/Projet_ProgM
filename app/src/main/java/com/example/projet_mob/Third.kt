package com.example.projet_mob

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class Third : Activity() {

    private lateinit var questionTextView: TextView
    private lateinit var cocktailImageView: ImageView
    private lateinit var feedbackTextView: TextView
    private lateinit var choice1: Button
    private lateinit var choice2: Button
    private lateinit var choice3: Button
    private lateinit var choice4: Button
    private lateinit var point : TextView
    private var score : Int = 0
    private lateinit var nextButton: Button


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

        questionTextView = findViewById(R.id.questionTextView)
        cocktailImageView = findViewById(R.id.cocktailImageView)
        feedbackTextView = findViewById(R.id.feedbackTextView)
        choice1 = findViewById(R.id.choice1)
        choice2 = findViewById(R.id.choice2)
        choice3 = findViewById(R.id.choice3)
        choice4 = findViewById(R.id.choice4)
        point = findViewById(R.id.point)
        nextButton = findViewById(R.id.btnNext)
        nextButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }


        loadQuestion()

        // Ajouter des listeners aux boutons
        val buttons = listOf(choice1, choice2, choice3, choice4)
        for (button in buttons) {
            button.setOnClickListener {
                checkAnswer(button.text.toString())
            }
        }
    }

    private fun loadQuestion() {
        point.text = "Score : ${score}"
        val cocktail = cocktails[currentCocktailIndex]
        questionTextView.text = "Quel est l'alcool dans ${cocktail.name} ?"
        cocktailImageView.setImageResource(cocktail.imageRes)
        correctAnswer = cocktail.correctAlcohol

        // Mélanger les réponses
        val options = mutableListOf("Vodka", "Gin", "Rhum", "Tequila").shuffled()
        choice1.text = options[0]
        choice2.text = options[1]
        choice3.text = options[2]
        choice4.text = options[3]
    }

    private fun checkAnswer(selectedAnswer: String) {
        if (selectedAnswer == correctAnswer) {
            feedbackTextView.text = "Bonne réponse ! 🎉"
            Toast.makeText(this, "Bonne réponse !", Toast.LENGTH_SHORT).show()
            score += 1
        } else {
            feedbackTextView.text = "Mauvaise réponse... 😞 C'était $correctAnswer"
            Toast.makeText(this, "Mauvaise réponse !", Toast.LENGTH_SHORT).show()
        }

        currentCocktailIndex++

        if (currentCocktailIndex < cocktails.size) {
            // Continuer le quiz après une petite pause
            feedbackTextView.postDelayed({ loadQuestion() }, 2000)
        } else {
            // Fin du quiz
            feedbackTextView.postDelayed({
                showEndScreen()
            }, 2000)
        }
    }

    private fun showEndScreen() {
        questionTextView.text = "🎉 Quiz terminé !"
        cocktailImageView.setImageResource(0) // efface l’image
        choice1.visibility = Button.GONE
        choice2.visibility = Button.GONE
        choice3.visibility = Button.GONE
        choice4.visibility = Button.GONE
        point.visibility = TextView.GONE
        feedbackTextView.text = "Votre score final : $score / ${cocktails.size}"
        nextButton.visibility = Button.VISIBLE
    }

    data class Cocktail(val name: String, val imageRes: Int, val correctAlcohol: String)
}
