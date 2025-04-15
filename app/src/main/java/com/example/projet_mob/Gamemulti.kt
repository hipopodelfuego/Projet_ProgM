package com.example.projet_mob

import androidx.compose.runtime.mutableStateOf

class Gamemulti {
    var randomSequence = mutableListOf<String>()
    var currentIndex = mutableStateOf(0)
    var totalScore = mutableStateOf(0)

    fun startNewGame(activities: List<String>) {
        randomSequence = activities.shuffled().take(3).toMutableList()
        currentIndex.value = 0
        totalScore.value = 0
        launchNextChallenge()
    }

    fun launchNextChallenge() {
        if (currentIndex.value < randomSequence.size) {
            // Lancer activité correspondante
        } else {
            // Fin du jeu
        }
    }

    fun updateScore(isCorrect: Boolean) {
        if (isCorrect) totalScore.value += 10
    }
}
