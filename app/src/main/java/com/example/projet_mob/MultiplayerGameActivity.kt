package com.example.projet_mob

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    private fun launchNextActivity() {
        if (currentIndex < challengeSequence.size) {
            val intent = Intent(this, challengeSequence[currentIndex])
            startActivityForResult(intent, coderequest)
        } else {
            BluetoothManager.bluetoothService.sendMessage("SCORE:$totalScore")
            waitForOpponentScore()
            endGameWithScore(totalScore)
        }
    }

    private fun waitForOpponentScore() {
        Log.d("Multiplayer", "Waiting for opponent score...")
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("Multiplayer", "Current opponent score: $opponentScore")
            if (opponentScore >= 0) {
                val win = totalScore > opponentScore
                BluetoothManager.bluetoothService.sendMessage(if (win) "WINNER" else "LOSER")
                goToResultScreen(win)
            } else {
                // Attendre encore si l'adversaire n'a pas envoyé son score
                waitForOpponentScore()
            }
        }, 1000)
    }

    private fun goToResultScreen(win: Boolean) {
        val intent = Intent(this, FinalScoreActivity::class.java)
        intent.putExtra("totalScore", totalScore)
        intent.putExtra("opponentScore", opponentScore)
        intent.putExtra("won", win)
        startActivity(intent)
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
    fun endGameWithScore(score: Int) {
        runOnUiThread {
            MultiplayerGameState.localScore = score
            BluetoothManager.bluetoothService?.sendMessage("SCORE:$score")

            // Afficher le score local immédiatement
            Toast.makeText(
                this,
                "Votre score envoyé: $score",
                Toast.LENGTH_LONG
            ).show()

            tryFinishGame()
        }
    }

    fun tryFinishGame() {
        if (MultiplayerGameState.bothScoresReceived() && !MultiplayerGameState.victorySent) {
            val won = MultiplayerGameState.isWinner()
            MultiplayerGameState.victorySent = true

            BluetoothManager.bluetoothService?.sendMessage(if (won) "WINNER" else "LOSER")

            val intent = Intent(this, FinalScoreActivity::class.java).apply {
                putExtra("totalScore", totalScore)
                putExtra("opponentScore", MultiplayerGameState.opponentScore)
                putExtra("won", won)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun handleBluetoothMessage(message: String) {
        Log.d("GameFlow", "Message reçu: $message")

        when {
            message.startsWith("SCORE:") -> {
                val score = message.removePrefix("SCORE:").toIntOrNull() ?: run {
                    Log.e("GameFlow", "Score invalide reçu: $message")
                    return
                }

                runOnUiThread {
                    MultiplayerGameState.opponentScore = score
                    Toast.makeText(
                        this,
                        "Score adversaire reçu: $score",
                        Toast.LENGTH_LONG
                    ).show()

                    tryFinishGame()
                }
            }
            message == "WINNER" -> goToResultScreen(false) // L'autre joueur a gagné
            message == "LOSER" -> goToResultScreen(true) // Nous avons gagné
        }
    }


}
