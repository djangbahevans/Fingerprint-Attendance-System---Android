package com.example.clockin.ui.configurewifi

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.clockin.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_wifi.*
import org.json.JSONObject

class WifiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi)

        acceptButton.setOnClickListener {
            if (ssidEditText.text.isEmpty()) {
                Snackbar.make(it, "SSID is empty", Snackbar.LENGTH_INDEFINITE).show()
            } else {
                if (passwordEditText.text.isEmpty()) {
                    Snackbar.make(it, "Password is empty", Snackbar.LENGTH_INDEFINITE).show()
                } else {
                    val jsonObject = JSONObject().apply {
                        put("ins", "setWiFi")
                        put("ssid", ssidEditText.text)
                        put("password", passwordEditText.text)
                    }

                    val result = jsonObject.toString()
                    val intent = intent.putExtra("message", result)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }
}
