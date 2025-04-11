package com.example.projet_mob

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

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


    data class Question(
        val text: String,
        val options: List<Int>, // liste des images
        val correctImageRes: Int
    )

    private val questions = listOf(
        Question("Quel est ce bar : la maison", listOf(R.drawable.maison, R.drawable.annexe, R.drawable.leptitvelo, R.drawable.vb), R.drawable.maison),
        Question("Quel est ce bar : l'annexe", listOf(R.drawable.annexe, R.drawable.baratin, R.drawable.lezing, R.drawable.uzine), R.drawable.annexe),
        Question("Quel est ce bar : le petit velo", listOf(R.drawable.leptitvelo, R.drawable.lezing, R.drawable.cave, R.drawable.maison), R.drawable.leptitvelo),
        Question("Quel est ce bar : le delirium", listOf(R.drawable.deli, R.drawable.typh, R.drawable.maison, R.drawable.baratin), R.drawable.deli),
        Question("Quel est ce bar : le zing", listOf(R.drawable.lezing, R.drawable.uzine, R.drawable.vb, R.drawable.annexe), R.drawable.lezing),
        Question("Quel est ce bar : le baratin", listOf(R.drawable.baratin, R.drawable.typh, R.drawable.leptitvelo, R.drawable.cave), R.drawable.baratin),
        Question("Quel est ce bar : l'uzine'", listOf(R.drawable.uzine, R.drawable.leptitvelo, R.drawable.baratin, R.drawable.annexe), R.drawable.uzine),
        Question("Quel est ce bar : Tiffany's Pub", listOf(R.drawable.typh, R.drawable.deli, R.drawable.maison, R.drawable.cave), R.drawable.typh),
        Question("Quel est ce bar : la cave à flo", listOf(R.drawable.cave, R.drawable.vb, R.drawable.deli, R.drawable.uzine), R.drawable.cave),
        Question("Quel est ce bar : Le V&B", listOf(R.drawable.vb, R.drawable.uzine, R.drawable.deli, R.drawable.baratin), R.drawable.vb),
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

        val options = listOf(option1, option2, option3, option4)

        options.forEach { imageView ->
            imageView.setOnClickListener {
                checkAnswer((it as ImageView).tag as Int)
            }
        }

        loadQuestion()
    }

    private fun loadQuestion() {
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
    }

    private fun checkAnswer(selectedImageRes: Int) {
        if (selectedImageRes == correctImageRes) {
            feedbackTextView.text = "Bonne réponse 🎉"
            Toast.makeText(this, "Correct !", Toast.LENGTH_SHORT).show()
            score++
        } else {
            feedbackTextView.text = "Mauvaise réponse 😞"
            Toast.makeText(this, "Incorrect", Toast.LENGTH_SHORT).show()
        }

        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size
        feedbackTextView.postDelayed({ loadQuestion() }, 2000)
    }

    //data class Cocktail(val name: String, val imageRes: Int, val correctAlcohol: String)
}
