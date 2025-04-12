package com.example.projet_mob

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class RandomGameActivity : Activity() {

    private val allGames = listOf(
        First::class.java,
        Second::class.java,
        Third::class.java,
        Fourth::class.java,
        Fifth::class.java,
        Sixth::class.java
    )

    private lateinit var selectedGames: List<Class<out Activity>>
    private var currentGameIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sélectionne 4 activités aléatoires
        selectedGames = allGames.shuffled().take(4)
        launchNextGame()
    }

    private fun launchNextGame() {
        if (currentGameIndex < selectedGames.size) {
            val intent = Intent(this, selectedGames[currentGameIndex])
            startActivityForResult(intent, 123)
        } else {
            // Quand les 4 jeux sont finis, on termine RandomGameActivity
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Passe au jeu suivant
        currentGameIndex++
        launchNextGame()
    }
}
