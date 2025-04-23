package com.example.projet_mob

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.projet_mob.activity.*
import com.example.projet_mob.bluetooth.BluetoothManager

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
        BluetoothManager.bluetoothService?.onMessageReceivedCallback = { message ->
            handleBluetoothMessage(message)
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
        MultiplayerGameState.localScore = score
        BluetoothManager.bluetoothService?.sendMessage("SCORE:$score")

        tryFinishGame()
    }
    fun tryFinishGame() {
        if (MultiplayerGameState.bothScoresReceived() && !MultiplayerGameState.victorySent) {
            val won = MultiplayerGameState.isWinner()  // Déterminer si le joueur a gagné
            MultiplayerGameState.victorySent = true

            // Envoyer le message de victoire ou défaite
            if (won) {
                BluetoothManager.bluetoothService?.sendMessage("WINNER")
            } else {
                BluetoothManager.bluetoothService?.sendMessage("LOSER")
            }

            // Afficher l'écran final avec les scores
            val intent = Intent(this, FinalScoreActivity::class.java)
            intent.putExtra("totalScore", MultiplayerGameState.localScore!! + MultiplayerGameState.opponentScore!!)
            intent.putExtra("won", won)
            startActivity(intent)
            finish()
        }
    }

    private fun handleBluetoothMessage(message: String) {
        when {
            message.startsWith("SCORE:") -> {  // Si c'est un score
                val score = message.removePrefix("SCORE:").toIntOrNull()
                if (score != null) {
                    MultiplayerGameState.opponentScore = score
                    tryFinishGame()  // Vérifier si on peut finir le jeu
                }
            }
            message == "WINNER" -> {  // Si c'est un message de victoire
                goToResultScreen(true)
            }
            message == "LOSER" -> {  // Si c'est un message de défaite
                goToResultScreen(false)
            }
        }
    }


}
