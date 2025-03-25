package com.example.projet_mob

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val multiplayerMenu = Intent(this, MultiplayerMenu::class.java)
        val soloPlayer = Intent(this, SoloGame::class.java)

        val startMultiplayerMenu: Button = findViewById(R.id.btnMultijoueur)
        val startGameButton: Button = findViewById(R.id.btnSolo)

        startMultiplayerMenu.setOnClickListener {
            startActivity(multiplayerMenu)
        }

        startGameButton.setOnClickListener {
            startActivity(soloPlayer)
        }
    }
}
