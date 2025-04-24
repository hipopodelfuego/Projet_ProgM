package com.example.projet_mob

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class FinalScoreActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_score)

        val finalScore = intent.getIntExtra("totalScore", 0)
        val isMultiplayer = intent.getBooleanExtra("multiplayer", false)
        val won = intent.getBooleanExtra("won", false)

        val scoreText = findViewById<TextView>(R.id.scoreFinalText)
        val button = findViewById<Button>(R.id.returnMenuButton)

        scoreText.text = getString(R.string.score_cumul_text, finalScore)

        val soundRes = if (isMultiplayer) {
            if (won) R.raw.victory else R.raw.defeat
        } else {
            R.raw.end_music
        }

        mediaPlayer = MediaPlayer.create(this, soundRes).apply {
            start()
        }

        button.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
