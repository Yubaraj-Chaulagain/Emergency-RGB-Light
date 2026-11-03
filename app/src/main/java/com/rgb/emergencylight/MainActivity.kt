package com.rgb.emergencylight

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*

class MainActivity : Activity() {
    private lateinit var lightView: LightView
    private lateinit var controls: LinearLayout
    private lateinit var previewParams: LinearLayout.LayoutParams
    private lateinit var textInput: EditText
    private lateinit var root: LinearLayout
    private var fullScreen = false
    private val modes = arrayOf("Tihar Jhilmili", "Emergency", "Rainbow", "Disco", "Running Light", "Twinkle", "Wave", "Soft Glow", "Text Zigzag", "Text Scroll", "Text Bounce", "Text Rainbow", "Text Blink", "Text Rotate")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(8), dp(12), dp(10)); setBackgroundColor(Color.rgb(10,13,24)) }
        val title = TextView(this).apply { text="🌈 Emergency RGB Light"; textSize=22f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; setPadding(0,dp(5),0,dp(8)) }
        root.addView(title)
        lightView = LightView(this)
        previewParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        root.addView(lightView, previewParams)

        controls = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        textInput = EditText(this).apply { hint="Type your words here..."; setHintTextColor(Color.LTGRAY); setTextColor(Color.WHITE); setSingleLine(true); textSize=17f; setPadding(dp(10),0,dp(10),0) }
        controls.addView(textInput, LinearLayout.LayoutParams(-1, dp(50)))
        val textButtons = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER }
        textButtons.addView(Button(this).apply { text="SAVE WORD"; isAllCaps=false; setOnClickListener { saveWord() } }, LinearLayout.LayoutParams(0,dp(46),1f))
        textButtons.addView(Button(this).apply { text="MY SAVED WORDS"; isAllCaps=false; setOnClickListener { showSavedWords() } }, LinearLayout.LayoutParams(0,dp(46),1f))
        controls.addView(textButtons)
        val modeScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled=false }
        val modeButtons = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        modes.forEachIndexed { index, name ->
            modeButtons.addView(Button(this).apply { text=name; isAllCaps=false; setOnClickListener { lightView.setDisplayText(textInput.text.toString()); lightView.setMode(index) } }, LinearLayout.LayoutParams(-2,dp(46)).apply { setMargins(dp(2),dp(2),dp(2),dp(2)) })
        }
        modeScroll.addView(modeButtons); controls.addView(modeScroll, LinearLayout.LayoutParams(-1,dp(52)))
        addSlider(controls,"Bulb / Text size",35) { lightView.setBulbSize(it) }
        addSlider(controls,"Brightness",100) { lightView.setBrightness(it) }
        addSlider(controls,"Light speed",55) { lightView.setSpeed(it) }
        val actionRow = LinearLayout(this).apply { gravity=Gravity.CENTER }
        actionRow.addView(Button(this).apply { text="STOP"; setOnClickListener { lightView.stopLight() } }, LinearLayout.LayoutParams(0,dp(48),1f))
        actionRow.addView(Button(this).apply { text="START"; setOnClickListener { lightView.startLight() } }, LinearLayout.LayoutParams(0,dp(48),1f))
        actionRow.addView(Button(this).apply { text="FULL SCREEN"; isAllCaps=false; setOnClickListener { toggleFullScreen(title) } }, LinearLayout.LayoutParams(0,dp(48),1f))
        controls.addView(actionRow)
        root.addView(controls)
        setContentView(root)
    }

    private fun addSlider(parent: LinearLayout, label: String, initial: Int, changed: (Int)->Unit) {
        parent.addView(TextView(this).apply { text=label; textSize=14f; setTextColor(Color.WHITE) })
        parent.addView(SeekBar(this).apply { max=100; progress=initial; setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar:SeekBar?, progress:Int, fromUser:Boolean) { changed(progress) }
            override fun onStartTrackingTouch(seekBar:SeekBar?) {}
            override fun onStopTrackingTouch(seekBar:SeekBar?) {}
        }) })
    }
    private fun saveWord() {
        val word=textInput.text.toString().trim()
        if(word.isEmpty()){ Toast.makeText(this,"Please type a word first",Toast.LENGTH_SHORT).show(); return }
        val prefs=getSharedPreferences("saved_words",MODE_PRIVATE)
        val set=prefs.getStringSet("words", emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(word); prefs.edit().putStringSet("words",set).apply()
        lightView.setDisplayText(word); Toast.makeText(this,"Word saved",Toast.LENGTH_SHORT).show()
    }
    private fun showSavedWords() {
        val words=getSharedPreferences("saved_words",MODE_PRIVATE).getStringSet("words",emptySet())?.toList()?.sorted() ?: emptyList()
        if(words.isEmpty()){ AlertDialog.Builder(this).setMessage("No saved words yet.").setPositiveButton("OK",null).show(); return }
        AlertDialog.Builder(this).setTitle("Choose a saved word").setItems(words.toTypedArray()) { _, which ->
            val selected=words[which]; textInput.setText(selected); lightView.setDisplayText(selected)
            AlertDialog.Builder(this).setMessage(selected).setPositiveButton("Use",null).setNeutralButton("Delete") { _, _ ->
                val set=getSharedPreferences("saved_words",MODE_PRIVATE).getStringSet("words",emptySet())?.toMutableSet() ?: mutableSetOf(); set.remove(selected); getSharedPreferences("saved_words",MODE_PRIVATE).edit().putStringSet("words",set).apply(); Toast.makeText(this,"Deleted",Toast.LENGTH_SHORT).show()
            }.show()
        }.setNegativeButton("Close",null).show()
    }
    private fun toggleFullScreen(title: TextView) {
        fullScreen=!fullScreen
        controls.visibility=if(fullScreen) View.GONE else View.VISIBLE
        title.visibility=if(fullScreen) View.GONE else View.VISIBLE
        root.setPadding(if(fullScreen) 0 else dp(12),if(fullScreen) 0 else dp(8),if(fullScreen) 0 else dp(12),if(fullScreen) 0 else dp(10))
        window.decorView.systemUiVisibility=if(fullScreen) (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) else View.SYSTEM_UI_FLAG_VISIBLE
    }
    private fun dp(value:Int)=(value*resources.displayMetrics.density).toInt()
    override fun onDestroy(){ if(::lightView.isInitialized) lightView.stopLight(); super.onDestroy() }
}
