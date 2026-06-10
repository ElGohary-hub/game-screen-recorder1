package com.game.recorder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private var isRecording = false
    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var recordButton: Button
    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // تعريف مدير تسجيل الشاشة بتاع أندرويد
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(50, 80, 50, 50)
        }

        val titleText = TextView(this).apply {
            text = "لوحة تحكم المسجل الاحترافي"
            textSize = 22f
            setTextColor(Color.WHITE)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
        rootLayout.addView(titleText)

        addSpacer(rootLayout, 60)

        val typeLabel = TextView(this).apply { text = "نوع التسجيل:"; textSize = 16f; setTextColor(Color.LTGRAY) }
        rootLayout.addView(typeLabel)

        val modeGroup = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL }
        val videoRadio = RadioButton(this).apply { text = "شاشة وصوت"; setTextColor(Color.WHITE); isChecked = true }
        val audioRadio = RadioButton(this).apply { text = "صوت فقط"; setTextColor(Color.WHITE) }
        modeGroup.addView(videoRadio)
        modeGroup.addView(audioRadio)
        rootLayout.addView(modeGroup)

        addSpacer(rootLayout, 40)

        val filterLabel = TextView(this).apply { text = "وضع فلاتر الرسوميات:"; textSize = 16f; setTextColor(Color.LTGRAY) }
        rootLayout.addView(filterLabel)

        val filterGroup = RadioGroup(this).apply { orientation = LinearLayout.VERTICAL }
        val noFilter = RadioButton(this).apply { text = "بدون فلاتر"; setTextColor(Color.WHITE); isChecked = true }
        val liveFilter = RadioButton(this).apply { text = "فلتر مباشر أثناء اللعب"; setTextColor(Color.WHITE) }
        val postFilter = RadioButton(this).apply { text = "تطبيق الفلتر بعد التسجيل"; setTextColor(Color.WHITE) }
        filterGroup.addView(noFilter)
        filterGroup.addView(liveFilter)
        filterGroup.addView(postFilter)
        rootLayout.addView(filterGroup)

        addSpacer(rootLayout, 80)

        recordButton = Button(this).apply {
            text = "ابدأ التسجيل"
            setBackgroundColor(Color.parseColor("#6200EE"))
            setTextColor(Color.WHITE)
            textSize = 18f
            setPadding(20, 40, 20, 40)
        }

        recordButton.setOnClickListener {
            if (!isRecording) {
                // طلب صلاحية تصوير الشاشة من أندرويد
                val captureIntent = projectionManager.createScreenCaptureIntent()
                startActivityForResult(captureIntent, RECORD_REQUEST_CODE)
            } else {
                // إيقاف التسجيل
                isRecording = false
                recordButton.text = "ابدأ التسجيل"
                recordButton.setBackgroundColor(Color.parseColor("#6200EE"))
                Toast.makeText(this, "تم إيقاف التسجيل!", Toast.LENGTH_LONG).show()
            }
        }
        rootLayout.addView(recordButton)

        setContentView(rootLayout)
    }

    // هنا بنستقبل رد المستخدم (وافق ولا رفض تصوير الشاشة؟)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                isRecording = true
                recordButton.text = "إيقاف وحفظ التسجيل"
                recordButton.setBackgroundColor(Color.RED)
                Toast.makeText(this, "تم أخذ الصلاحية! (جاهزين نشغل المحرك)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "تم رفض الصلاحية من المستخدم", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addSpacer(layout: LinearLayout, height: Int) {
        val spacer = TextView(this)
        spacer.height = height
        layout.addView(spacer)
    }
}
