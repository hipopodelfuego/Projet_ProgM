import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.projet_mob.FinalScoreActivity
import com.example.projet_mob.R
import com.example.projet_mob.activity.*
import com.example.projet_mob.bluetooth.BluetoothManager
import com.example.projet_mob.bluetooth.MyBluetoothService

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

        fun setOpponentScore(score: Int) {
            opponentScore = score
        }

        fun setVictory(win: Boolean) {
            hasWon = win
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_game)

        val ids = intent.getIntArrayExtra("challenge_ids") ?: intArrayOf(0, 1, 2)
        challengeSequence = ids.map { activityMap[it] }

        launchNextActivity()
    }

    private fun launchNextActivity() {
        if (currentIndex < challengeSequence.size) {
            val intent = Intent(this, challengeSequence[currentIndex])
            startActivityForResult(intent, coderequest)
        } else {
            // Fin du jeu -> envoyer score
            BluetoothManager.bluetoothService.sendMessage("SCORE:$totalScore")
            waitForOpponentScore()
        }
    }

    private fun waitForOpponentScore() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (opponentScore >= 0) {
                val win = totalScore > opponentScore
                BluetoothManager.bluetoothService.sendMessage(if (win) "WINNER" else "LOSER")
                goToResultScreen(win)
            } else {
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
}
