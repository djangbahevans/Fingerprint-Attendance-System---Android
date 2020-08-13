package com.example.clockin.ui.deleteemployee

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.clockin.Constants
import com.example.clockin.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_delete_employee.*
import org.json.JSONObject

class DeleteEmployeeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_employee)

        delete.setOnClickListener {
            if (employee_id.text.isNotEmpty()) {
                val jsonObject = JSONObject().apply {
                    put("ins", "delete")
                    put("id", employee_id.text.toString().toInt())
                }
                val queue = Volley.newRequestQueue(this)
                val request = JsonObjectRequest(Request.Method.POST, Constants.URL, jsonObject, {
                    val intent = Intent().apply {
                        putExtra("message", jsonObject.toString())
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }, {
                    val intent = intent.putExtra("message", "")
                    setResult(Activity.RESULT_FIRST_USER, intent)
                    finish()
                })
                queue.add(request)
            } else {
                Snackbar.make(it, "No employee ID specified", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
