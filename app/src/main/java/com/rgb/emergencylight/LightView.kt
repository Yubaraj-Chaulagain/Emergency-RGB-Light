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
import kotlin.math.abs
import kotlin.math.sin

class LightView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    }
    private val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.rgb(255, 125, 0), Color.WHITE)
    private var mode = 0
    private var speed = 55
    private var bulbScale = 0.75f
    private var brightness = 100
    private var running = false
    private var frame = 0
    private var displayText = "YOUR TEXT"
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
    init { setLayerType(View.LAYER_TYPE_SOFTWARE, null) }
    fun setMode(value: Int) { mode = value; invalidate() }
    fun setDisplayText(value: String) { displayText = value.ifBlank { "YOUR TEXT" }; invalidate() }
    fun setBulbSize(value: Int) { bulbScale = 0.45f + value.coerceIn(0, 100) / 100f; invalidate() }
    fun setBrightness(value: Int) { brightness = value.coerceIn(10, 100); invalidate() }
    fun setSpeed(value: Int) { speed = value.coerceIn(0, 100) }
    fun startLight() { if (running) return; running = true; handler.removeCallbacks(task); handler.post(task) }
    fun stopLight() { running = false; handler.removeCallbacks(task); invalidate() }
    override fun onAttachedToWindow() { super.onAttachedToWindow(); startLight() }
    override fun onDetachedFromWindow() { stopLight(); super.onDetachedFromWindow() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        canvas.drawColor(Color.BLACK)
        when (mode) {
            0 -> drawTihar(canvas, w, h)
            1 -> drawEmergency(canvas, w, h)
            2 -> drawRainbow(canvas, w, h)
            3 -> drawDisco(canvas, w, h)
            4 -> drawRunning(canvas, w, h)
            5 -> drawTwinkle(canvas, w, h)
            6 -> drawWave(canvas, w, h)
            7 -> drawSoftGlow(canvas, w, h)
            else -> drawTextEffect(canvas, w, h)
        }
    }

    private fun drawTextEffect(canvas: Canvas, w: Float, h: Float) {
        val effect = mode - 8
        val text = displayText
        val baseSize = (w * 0.13f).coerceAtMost(h * 0.30f).coerceAtLeast(24f)
        textPaint.textSize = baseSize
        val maxWidth = w * 0.88f
        var textWidth = textPaint.measureText(text)
        // Keep long messages readable instead of shrinking them until they become tiny.
        // Long text automatically uses a continuous right-to-left marquee.
        val isLongText = textWidth > maxWidth
        if (!isLongText) {
            while (textWidth > maxWidth && textPaint.textSize > 18f) {
                textPaint.textSize *= 0.92f
                textWidth = textPaint.measureText(text)
            }
        }
        val centerY = h / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
        val marqueeX = w + textWidth / 2f - ((frame * (2f + speed / 18f)) % (w + textWidth))
        val x = when {
            isLongText -> marqueeX
            effect == 1 -> ((frame * (2f + speed / 18f)) % (w + textWidth)) - textWidth / 2f
            effect == 2 || effect == 3 || effect == 4 -> w / 2f
            else -> w / 2f
        }
        val y = when (effect) {
            0 -> centerY + ((frame % 8) - 4) * 5f
            2 -> centerY + abs(sin(frame * 0.12) * h * 0.22).toFloat() * if (frame % 2 == 0) 1 else -1
            else -> centerY
        }
        val color = when (effect) {
            3 -> colors[(frame / 2) % colors.size]
            4 -> if (frame % 2 == 0) Color.WHITE else Color.TRANSPARENT
            else -> colors[(frame / 3) % colors.size]
        }
        if (effect == 4 && frame % 2 != 0) return
        textPaint.color = color
        textPaint.alpha = brightness * 255 / 100
        textPaint.style = Paint.Style.STROKE
        textPaint.strokeWidth = (textPaint.textSize * 0.10f).coerceAtLeast(3f)
        textPaint.setShadowLayer(textPaint.textSize * 0.45f, 0f, 0f, color)
        if (effect == 5) {
            canvas.save(); canvas.rotate(frame * 2f, w / 2f, h / 2f)
            textPaint.style = Paint.Style.FILL
            canvas.drawText(text, w / 2f, centerY, textPaint)
            canvas.restore()
        } else {
            canvas.drawText(text, x, y, textPaint)
        }
        textPaint.clearShadowLayer()
        textPaint.style = Paint.Style.FILL
    }

    private fun drawTihar(canvas: Canvas, w: Float, h: Float) { for (i in 0 until 42) { val x=(i%7+0.5f)*w/7f; val y=(i/7+0.5f)*h/6f; drawBulb(canvas,x,y,colors[(i+frame)%colors.size],11f) } }
    private fun drawEmergency(canvas: Canvas, w: Float, h: Float) { paint.color=if(frame%2==0) Color.RED else Color.BLUE; canvas.drawRect(0f,0f,w,h,paint); paint.color=Color.WHITE; canvas.drawCircle(w/2,h/2,w.coerceAtMost(h)*0.12f,paint) }
    private fun drawRainbow(canvas: Canvas, w: Float, h: Float) { for(i in 0 until 7){paint.color=colors[(i+frame/2)%colors.size];canvas.drawRect(i*w/7f,0f,(i+1)*w/7f+1,h,paint)} }
    private fun drawDisco(canvas: Canvas,w:Float,h:Float){val r=Random(frame/2);repeat(28){drawBulb(canvas,r.nextFloat()*w,r.nextFloat()*h,colors[r.nextInt(colors.size)],8f+r.nextFloat()*15f)}}
    private fun drawRunning(canvas:Canvas,w:Float,h:Float){for(i in 0 until 12){val x=((i+frame)%12+0.5f)*w/12f;drawBulb(canvas,x,h/2,colors[i%colors.size],16f)}}
    private fun drawTwinkle(canvas:Canvas,w:Float,h:Float){val r=Random(frame/2);repeat(64){drawBulb(canvas,(it%8+0.5f)*w/8f,(it/8+0.5f)*h/8f,colors[r.nextInt(colors.size)],4.5f+r.nextFloat()*2f)}}
    private fun drawWave(canvas:Canvas,w:Float,h:Float){for(i in 0 until 36){val x=(i+0.5f)*w/36f;val y=h*(0.5f+0.32f*sin(i*0.35+frame*0.12).toFloat());drawBulb(canvas,x,y,colors[(i+frame/3)%colors.size],5.5f)}}
    private fun drawSoftGlow(canvas:Canvas,w:Float,h:Float){for(i in 0 until 48){drawBulb(canvas,(i%8+0.5f)*w/8f,(i/8+0.5f)*h/6f,colors[(i+frame/5)%colors.size],5f)}}
    private fun drawBulb(canvas:Canvas,x:Float,y:Float,color:Int,radius:Float){val c=(color and 0x00FFFFFF) or ((brightness*255/100) shl 24);val r=radius*bulbScale;paint.color=c;paint.shader=RadialGradient(x,y,r*2.4f,intArrayOf(c,c and 0x00FFFFFF,Color.TRANSPARENT),floatArrayOf(0f,0.45f,1f),Shader.TileMode.CLAMP);canvas.drawCircle(x,y,r*2.4f,paint);paint.shader=null;paint.color=c;canvas.drawCircle(x,y,r*0.55f,paint)}
}
