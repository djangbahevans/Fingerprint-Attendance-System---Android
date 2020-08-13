package com.example.clockin.ui.addemployee

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.clockin.Constants
import com.example.clockin.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_employee.*
import org.json.JSONObject

class AddEmployeeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_employee)

        acceptButton.setOnClickListener {
            if (idTextEdit.text.isEmpty()) {
                Snackbar.make(it, "Index is empty", Snackbar.LENGTH_INDEFINITE).show()
            } else {
                if (firstName.text.isEmpty()) {
                    Snackbar.make(
                        firstName,
                        "First name is empty",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .show()
                } else {
                    if (lastName.text.isEmpty()) {
                        Snackbar.make(
                            it,
                            "Last name is empty",
                            Snackbar.LENGTH_INDEFINITE
                        ).show()
                    } else {
                        if (departmentTextEdit.text.isEmpty()) {
                            Snackbar.make(
                                it,
                                "Department is empty",
                                Snackbar.LENGTH_INDEFINITE
                            ).show()
                        } else {
                            it.isEnabled = false
                            val queue = Volley.newRequestQueue(this)
                            val jsonObject = JSONObject().apply {
                                put("ins", "add")
                                put("id", idTextEdit.text.toString().toInt())
                                put("first", firstName.text)
                                put("last", lastName.text)
                                put("dept", departmentTextEdit.text)
                            }
                            val request = JsonObjectRequest(
                                Request.Method.POST,
                                Constants.URL,
                                jsonObject,
                                {
                                    jsonObject.remove("first")
                                    jsonObject.remove("last")
                                    jsonObject.remove("dept")
                                    val result = jsonObject.toString()
                                    val intent = intent.putExtra("message", result)
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                },
                                {
                                    val intent = intent.putExtra("message", "")
                                    setResult(Activity.RESULT_FIRST_USER, intent)
                                    finish()
                                })
                            queue.add(request)
                        }
                    }
                }
            }
        }
    }
}
