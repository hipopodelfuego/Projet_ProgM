package com.example.projet_mob

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import kotlin.random.Random

class SoloGame : Activity() {

    private val activities = listOf(
        //First::class.java,
        Second::class.java,
        Third::class.java,
        Fourth::class.java,
        Fifth::class.java,
        Sixth::class.java
    )
    private var totalScore = 0
    private lateinit var randomSequence: List<Class<out Activity>>
    private var currentIndex = 0
    private val REQUEST_CODE = 1234  // un code arbitraire

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sologame)

        findViewById<Button>(R.id.btn1).setOnClickListener {
            startActivity(Intent(this, First::class.java))
        }
        findViewById<Button>(R.id.btn2).setOnClickListener {
            startActivity(Intent(this, Second::class.java))
        }
        findViewById<Button>(R.id.btn3).setOnClickListener {
            startActivity(Intent(this, Third::class.java))
        }
        findViewById<Button>(R.id.btn4).setOnClickListener {
            startActivity(Intent(this, Fourth::class.java))
        }
        findViewById<Button>(R.id.btn5).setOnClickListener {
            startActivity(Intent(this, Fifth::class.java))
        }
        findViewById<Button>(R.id.btn6).setOnClickListener {
            startActivity(Intent(this, Sixth::class.java))
        }

        // BOUTON ALÉATOIRE
        findViewById<Button>(R.id.btnRandom).setOnClickListener {
            // Choisir 4 activités aléatoires
            randomSequence = activities.shuffled().take(3)
            currentIndex = 0
            launchNextActivity()
        }
        startSecondActivity.setOnClickListener {
            val second = Intent(this, Second::class.java)
            startActivity(second)
        }
        startFourthActivity.setOnClickListener {
            val fourth = Intent(this, Fourth::class.java)
            startActivity(fourth)
        }
        startThirdActivity.setOnClickListener({
            val third = Intent(this, Third::class.java)
            startActivity(third)
        })
    }

    private fun launchNextActivity() {
        if (currentIndex < randomSequence.size) {
            val intent = Intent(this, randomSequence[currentIndex])
            startActivityForResult(intent, REQUEST_CODE)
        } else {
            // 🎉 Fin du jeu aléatoire
            val intent = Intent(this, FinalScoreActivity::class.java)
            intent.putExtra("totalScore", totalScore)
            startActivity(intent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val score = data?.getIntExtra("score", 0) ?: 0
            totalScore += score
            currentIndex++
            launchNextActivity()
        }
    }
}
