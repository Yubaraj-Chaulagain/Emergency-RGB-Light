package com.rgb.emergencylight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import android.os.Handler
import android.os.Looper
import kotlin.random.Random

class LightView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colors = intArrayOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
        Color.CYAN, Color.MAGENTA, Color.rgb(255, 125, 0), Color.WHITE
    )
    private var mode = 0
    private var speed = 55
    private var running = true
    private var frame = 0
    private val handler = Handler(Looper.getMainLooper())
    private val task = object : Runnable {
        override fun run() {
            if (running) {
                frame++
                invalidate()
                handler.postDelayed(this, (220 - speed * 2).toLong().coerceAtLeast(20L))
            }
        }
    }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        startLight()
    }

    fun setMode(value: Int) {
        mode = value
        invalidate()
    }

    fun setSpeed(value: Int) {
        speed = value.coerceIn(0, 100)
    }

    fun startLight() {
        if (running) {
            handler.removeCallbacks(task)
        } else {
            running = true
        }
        handler.post(task)
    }

    fun stopLight() {
        running = false
        handler.removeCallbacks(task)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        canvas.drawColor(Color.BLACK)
        when (mode) {
            0 -> drawTihar(canvas, w, h)
            1 -> drawEmergency(canvas, w, h)
            2 -> drawRainbow(canvas, w, h)
            3 -> drawDisco(canvas, w, h)
            else -> drawRunning(canvas, w, h)
        }
    }

    private fun drawTihar(canvas: Canvas, w: Float, h: Float) {
        val count = 42
        for (i in 0 until count) {
            val x = (i % 7 + 0.5f) * w / 7f
            val row = i / 7
            val y = (row + 0.5f) * h / 6f
            drawBulb(canvas, x, y, colors[(i + frame) % colors.size], 11f)
        }
    }

    private fun drawEmergency(canvas: Canvas, w: Float, h: Float) {
        val color = if (frame % 2 == 0) Color.RED else Color.BLUE
        paint.color = color
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(w / 2f, h / 2f, (w.coerceAtMost(h) * 0.12f), paint)
    }

    private fun drawRainbow(canvas: Canvas, w: Float, h: Float) {
        val bands = 7
        for (i in 0 until bands) {
            paint.color = colors[(i + frame / 2) % colors.size]
            canvas.drawRect(i * w / bands, 0f, (i + 1) * w / bands + 1f, h, paint)
        }
    }

    private fun drawDisco(canvas: Canvas, w: Float, h: Float) {
        val random = Random(frame / 2)
        repeat(28) {
            val x = random.nextFloat() * w
            val y = random.nextFloat() * h
            drawBulb(canvas, x, y, colors[random.nextInt(colors.size)], 8f + random.nextFloat() * 15f)
        }
    }

    private fun drawRunning(canvas: Canvas, w: Float, h: Float) {
        val count = 12
        for (i in 0 until count) {
            val x = ((i + frame) % count + 0.5f) * w / count
            drawBulb(canvas, x, h / 2f, colors[i % colors.size], 16f)
        }
    }

    private fun drawBulb(canvas: Canvas, x: Float, y: Float, color: Int, radius: Float) {
        paint.color = color
        paint.shader = RadialGradient(
            x, y, radius * 2.4f,
            intArrayOf(color, color and 0x00FFFFFF, Color.TRANSPARENT),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(x, y, radius * 2.4f, paint)
        paint.shader = null
        paint.color = color
        canvas.drawCircle(x, y, radius * 0.55f, paint)
    }
}
