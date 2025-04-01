package com.example.projet_mob

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class SoloGame : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sologame)

        val startFirstActivity: Button = findViewById(R.id.btn1)
        val startSecondActivity: Button = findViewById(R.id.btn2)
        val startThirdActivity: Button = findViewById(R.id.btn3)

        startFirstActivity.setOnClickListener {
            val first = Intent(this, First::class.java)
            startActivity(first)
        }
        startSecondActivity.setOnClickListener {
            val second = Intent(this, Second::class.java)
            startActivity(second)
        }
        startThirdActivity.setOnClickListener({
            val third = Intent(this, Third::class.java)
            startActivity(third)
        })
    }
}