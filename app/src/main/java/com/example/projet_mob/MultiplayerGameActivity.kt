package com.example.projet_mob

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.projet_mob.activity.*
import com.example.projet_mob.ui.theme.BluetoothManager

class MultiplayerGameActivity : Activity() {

    private val activityMap = listOf(
        First::class.java,
        Second::class.java,
        Third::class.java,
        Fourth::class.java,
        Fifth::class.java,
        Sixth::class.java
    )

    private var challengeSequence = listOf<Class<out Activity>>()
    private var totalScore = 0
    private var currentIndex = 0
    private val coderequest = 1234

    companion object {
        var opponentScore = -1
        var hasWon = false

        fun updateOpponentScore(score: Int) {
            opponentScore = score
        }

        fun setVictory(win: Boolean) {
            hasWon = win
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_game)

        // Réinitialiser l'état du jeu
        MultiplayerGameState.reset()

        BluetoothManager.bluetoothService?.apply {
            onMessageReceivedCallback = { message ->
                runOnUiThread {
                    handleBluetoothMessage(message)
                }
            }
        }

        val ids = intent.getIntArrayExtra("challenge_ids") ?: intArrayOf(0, 1, 2)
        challengeSequence = ids.map { activityMap[it] }
        launchNextActivity()
    }
    private fun handleBluetoothMessage(message: String) {
        Log.d("GameFlow", "Message reçu: $message")

        when {
            message.startsWith("SCORE:") -> {
                val score = message.removePrefix("SCORE:").toIntOrNull() ?: return
                Log.d("GameFlow", "Score adverse reçu: $score")

                if (MultiplayerGameState.opponentScore == null) {
                    MultiplayerGameState.opponentScore = score
                    tryFinishGame()
                } else {
                    Log.d("GameFlow", "Score adverse déjà reçu, ignoré.")
                }
            }

            message == "WINNER" || message == "LOSER" -> {
                if (!finalScreenShown) {
                    val won = message == "LOSER" // Si l'autre dit qu'on est le perdant, alors on a gagné
                    Log.d("GameFlow", "Message de fin reçu: $message → won=$won")
                    showFinalScreen(won)
                } else {
                    Log.d("GameFlow", "Final screen déjà affiché, message de victoire/perte ignoré.")
                }
            }
        }
    }


    fun endGameWithScore(score: Int) {
        runOnUiThread {
            MultiplayerGameState.localScore = score
            BluetoothManager.bluetoothService?.sendMessage("SCORE:$score")
            tryFinishGame()
        }
    }

    fun tryFinishGame() {
        if (MultiplayerGameState.bothScoresReceived() && !MultiplayerGameState.victorySent) {
            MultiplayerGameState.victorySent = true
            val won = MultiplayerGameState.isWinner()

            BluetoothManager.bluetoothService?.sendMessage(if (won) "WINNER" else "LOSER")
            Log.d("GameFlow", "tryFinishGame triggered: local=${MultiplayerGameState.localScore}, opp=${MultiplayerGameState.opponentScore}")

            showFinalScreen(won)
        }
    }

    private var finalScreenShown = false

    private fun showFinalScreen(won: Boolean) {
        if (finalScreenShown) return
        finalScreenShown = true

        val intent = Intent(this, FinalScoreActivity::class.java).apply {
            putExtra("totalScore", MultiplayerGameState.localScore ?: 0)
            putExtra("opponentScore", MultiplayerGameState.opponentScore ?: 0)
            putExtra("won", won)
            putExtra("multiplayer", true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun launchNextActivity() {
        if (currentIndex < challengeSequence.size) {
            val intent = Intent(this, challengeSequence[currentIndex])
            startActivityForResult(intent, coderequest)
        } else {
            endGameWithScore(totalScore)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == coderequest && resultCode == RESULT_OK) {
            val score = data?.getIntExtra("score", 0) ?: 0
            totalScore += score
            currentIndex++
            launchNextActivity()
        }
    }

}
