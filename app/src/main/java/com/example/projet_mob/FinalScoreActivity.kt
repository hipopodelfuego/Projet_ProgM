package com.example.projet_mob

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class FinalScoreActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_score)

        val finalScore = intent.getIntExtra("totalScore", 0)
        val scoreText = findViewById<TextView>(R.id.scoreFinalText)
        val button = findViewById<Button>(R.id.returnMenuButton)

        scoreText.text = "Score cumulé : $finalScore"
        button.setOnClickListener {
            finish() // Ou revenir au menu principal
        }
    }
}
