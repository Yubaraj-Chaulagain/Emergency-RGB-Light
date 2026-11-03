package com.rgb.emergencylight

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var lightView: LightView

    private val modes = arrayOf(
        "Tihar Jhilmili", "Emergency", "Rainbow", "Disco", "Running Light", "Twinkle", "Wave", "Soft Glow"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(16))
            setBackgroundColor(Color.rgb(10, 13, 24))
        }

        val title = TextView(this).apply {
            text = "🌈 Emergency RGB Light"
            textSize = 23f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(12))
        }
        root.addView(title)

        lightView = LightView(this)
        root.addView(lightView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        val modeSpinner = Spinner(this)
        modeSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, modes
        )
        root.addView(modeSpinner, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
        ))
        modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                lightView.setMode(position)
            }
        }

        val speedLabel = TextView(this).apply {
            text = "Light speed"
            textSize = 16f
            setTextColor(Color.WHITE)
        }
        root.addView(speedLabel)

        val speedBar = SeekBar(this).apply {
            max = 100
            progress = 55
        }
        root.addView(speedBar)
        speedBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lightView.setSpeed(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val stop = Button(this).apply {
            text = "STOP"
            setOnClickListener { lightView.stopLight() }
        }
        val start = Button(this).apply {
            text = "START"
            setOnClickListener { lightView.startLight() }
        }
        buttons.addView(stop, LinearLayout.LayoutParams(0, dp(52), 1f))
        buttons.addView(start, LinearLayout.LayoutParams(0, dp(52), 1f))
        root.addView(buttons)

        setContentView(root)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        if (::lightView.isInitialized) lightView.stopLight()
        super.onDestroy()
    }
}
