package com.example.projet_mob

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView

class First : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private lateinit var tiltTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        // Initialisation des capteurs
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // On utilise l'accéléromètre

        // Récupération du TextView pour afficher l'inclinaison
        tiltTextView = findViewById(R.id.tiltTextView)
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = event.values[0] // Inclinaison gauche-droite
            val y = event.values[1] // Inclinaison avant-arrière

            // Affichage des valeurs pour voir l'inclinaison
            tiltTextView.text = "Inclinaison :\nGauche/Droite: ${x.toInt()}°\nAvant/Arrière: ${y.toInt()}°"

            // Vérification si l'utilisateur dépasse un certain seuil d'inclinaison
            if (Math.abs(x) > 10 || Math.abs(y) > 10) {
                tiltTextView.text = "💥 Verres Renversés ! 💥"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Pas besoin de gérer la précision ici
    }
}
