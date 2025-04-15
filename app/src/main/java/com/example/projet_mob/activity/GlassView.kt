package com.example.projet_mob.activity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GlassView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var fillHeight = 0f
    private var targetHeight = 0f

    private val paintGlass = Paint().apply {
        color = 0xFF000000.toInt() // Noir
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val paintWater = Paint().apply {
        color = 0xFF2196F3.toInt() // Bleu
        style = Paint.Style.FILL
    }

    private val paintTarget = Paint().apply {
        color = 0xFFFF0000.toInt() // Rouge
        strokeWidth = 4f
    }

    fun setFillHeight(height: Float) {
        fillHeight = height
        invalidate() // Redessiner la vue
    }

    fun setTargetHeight(height: Float) {
        targetHeight = height
        invalidate() // Redessiner la vue
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val glassWidth = width * 0.5f // Largeur du verre (50% de la vue)
        val left = (width - glassWidth) / 2 // Bord gauche
        val right = left + glassWidth // Bord droit

        val glassHeight = height * 0.6f // Ajustement pour centrer verticalement (60% de la vue)
        val top = (height - glassHeight) / 2 // Décalage vers le centre
        val bottom = top + glassHeight // Nouvelle position du bas du verre

        // Dessiner les bords verticaux du verre
        canvas.drawLine(left, bottom, left, top, paintGlass)  // Bord gauche vertical
        canvas.drawLine(right, bottom, right, top, paintGlass) // Bord droit vertical
        canvas.drawLine(left, bottom, right, bottom, paintGlass) // Base du verre

        // Dessiner l'eau
        val fillTop = bottom - fillHeight
        canvas.drawRect(left, fillTop, right, bottom, paintWater)

        // Dessiner la ligne cible
        val targetY = bottom - targetHeight
        canvas.drawLine(left, targetY, right, targetY, paintTarget)
    }

}
