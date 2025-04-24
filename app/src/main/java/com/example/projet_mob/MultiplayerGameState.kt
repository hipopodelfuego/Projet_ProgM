package com.example.projet_mob

object MultiplayerGameState {
    var playerScore: Int = 0
    var opponentScore: Int = 0
    var localScore: Int? = null
    var victorySent: Boolean = false

    // Vérifie si les scores des deux joueurs sont reçus
    fun bothScoresReceived(): Boolean {
        return localScore != null && opponentScore != null
    }

    // Vérifie si le joueur local a gagné
    fun isWinner(): Boolean {
        return localScore!! > opponentScore!!
    }
}
