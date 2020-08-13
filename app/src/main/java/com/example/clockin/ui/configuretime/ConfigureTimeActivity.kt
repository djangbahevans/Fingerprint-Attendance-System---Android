package com.example.clockin.ui.configuretime

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.clockin.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_configure_time.*
import org.json.JSONObject
import java.util.*

class ConfigureTimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_time)

        var timeSet = false
        var hour = 0
        var minute = 0

        open_time_pick_button.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val mHour = calendar.get(Calendar.HOUR)
            val mMinute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, hourSelected, minuteSelected ->
                    hour = hourSelected
                    minute = minuteSelected
                    val displayCurrentTime = findViewById<TextView>(R.id.display_current_time)
                    displayCurrentTime.text = getString(R.string.current_time_display).format(
                        hourSelected,
                        minuteSelected
                    )
                    timeSet = true
                },
                mHour,
                mMinute,
                true
            )
            timePickerDialog.show()
        }

        confirm_time_button.setOnClickListener {
            if (timeSet) {
                val jsonObject = JSONObject().apply {
                    put("ins", "changeTime")
                    put("hour", hour)
                    put("minute", minute)
                }
                val intent = Intent().apply {
                    putExtra(
                        "message",
                        jsonObject.toString()
                    )
                }
                setResult(Activity.RESULT_OK, intent)
            } else Snackbar.make(it, "Time not set", Snackbar.LENGTH_LONG).show()
            finish()
        }
    }
}
