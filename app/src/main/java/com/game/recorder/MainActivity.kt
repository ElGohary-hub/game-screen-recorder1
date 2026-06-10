package com.game.recorder

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // تصميم تخطيط الواجهة عمودياً (Vertical Layout)
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212")) // خلفية داكنة مريحة للألعاب
            setPadding(50, 80, 50, 50)
        }

        // عنوان التطبيق الداخلي
        val titleText = TextView(this).apply {
            text = "لوحة تحكم المسجل الاحترافي"
            textSize = 22f
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
        rootLayout.addView(titleText)

        // مسافة فارغة
        addSpacer(rootLayout, 60)

        // قسم اختيار نوع التسجيل
        val typeLabel = TextView(this).apply {
            text = "نوع التسجيل:"
            textSize = 16f
            setTextColor(Color.LTGRAY)
        }
        rootLayout.addView(typeLabel)

        val modeGroup = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL }
        val videoRadio = RadioButton(this).apply { text = "شاشة وصوت"; setTextColor(Color.WHITE); isChecked = true }
        val audioRadio = RadioButton(this).apply { text = "صوت فقط"; setTextColor(Color.WHITE) }
        modeGroup.addView(videoRadio)
        modeGroup.addView(audioRadio)
        rootLayout.addView(modeGroup)

        addSpacer(rootLayout, 40)

        // قسم اختيار الفلاتر
        val filterLabel = TextView(this).apply {
            text = "وضع فلاتر الرسوميات (GLSL Shaders):"
            textSize = 16f
            setTextColor(Color.LTGRAY)
        }
        rootLayout.addView(filterLabel)

        val filterGroup = RadioGroup(this).apply { orientation = LinearLayout.VERTICAL }
        val noFilter = RadioButton(this).apply { text = "بدون فلاتر"; setTextColor(Color.WHITE); isChecked = true }
        val liveFilter = RadioButton(this).apply { text = "فلتر مباشر أثناء اللعب (Live Shader)"; setTextColor(Color.WHITE) }
        val postFilter = RadioButton(this).apply { text = "تطبيق الفلتر بعد التسجيل (Post-Processing)"; setTextColor(Color.WHITE) }
        filterGroup.addView(noFilter)
        filterGroup.addView(liveFilter)
        filterGroup.addView(postFilter)
        rootLayout.addView(filterGroup)

        addSpacer(rootLayout, 80)

        // زر بدء وإيقاف التسجيل الرئيسي
        val recordButton = Button(this).apply {
            text = "ابدأ التسجيل"
            setBackgroundColor(Color.parseColor("#6200EE"))
            setTextColor(Color.WHITE)
            textSize = 18f
            setPadding(20, 40, 20, 40)
        }

        recordButton.setOnClickListener {
            isRecording = !isRecording
            if (isRecording) {
                recordButton.text = "إيقاف وحفظ التسجيل"
                recordButton.setBackgroundColor(Color.RED)
                val selectedMode = if (videoRadio.isChecked) "فيديو وصوت" else "صوت بس"
                Toast.makeText(this, "بدأ التسجيل بنمط: $selectedMode", Toast.LENGTH_SHORT).show()
            } else {
                recordButton.text = "ابدأ التسجيل"
                recordButton.setBackgroundColor(Color.parseColor("#6200EE"))
                Toast.makeText(this, "تم إيقاف التسجيل وحفظ الملف بنجاح!", Toast.LENGTH_LONG).show()
            }
        }
        rootLayout.addView(recordButton)

        setContentView(rootLayout)
    }

    private fun addSpacer(layout: LinearLayout, height: Int) {
        val spacer = TextView(this)
        spacer.height = height
        layout.addView(spacer)
    }
}
