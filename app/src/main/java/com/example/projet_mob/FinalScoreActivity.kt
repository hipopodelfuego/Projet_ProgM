package com.example.projet_mob

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.media.MediaPlayer

class FinalScoreActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_score)

        val finalScore = intent.getIntExtra("totalScore", 0)
        val scoreText = findViewById<TextView>(R.id.scoreFinalText)
        val button = findViewById<Button>(R.id.returnMenuButton)

        scoreText.text = getString(R.string.score_cumul_text, finalScore)

        mediaPlayer = MediaPlayer.create(this, R.raw.end_music)
        mediaPlayer?.start()

        button.setOnClickListener {
            finish() //revenir au menu principal
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
