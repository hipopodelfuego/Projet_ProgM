package com.example.projet_mob

object MultiplayerGameState {
    var playerScore: Int = 0
    var opponentScore: Int? = null
    var localScore: Int? = null
    var victorySent: Boolean = false

    fun reset() {
        playerScore = 0
        opponentScore = null
        localScore = null
        victorySent = false
    }

    fun bothScoresReceived(): Boolean {
        return localScore != null && opponentScore != null
    }

    fun isWinner(): Boolean {
        val local = localScore ?: return false
        val opponent = opponentScore ?: return false
        return local >= opponent
    }
}