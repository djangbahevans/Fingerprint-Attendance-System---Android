package com.example.clockin.ui.persondetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clockin.R
import com.example.clockin.ui.viewattendance.ViewAttendanceActivity
import kotlinx.android.synthetic.main.activity_person_detail.*
import java.time.LocalDateTime

class PersonDetailActivity : AppCompatActivity() {

    var attendance: ArrayList<LocalDateTime>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_detail)

        val name = intent.getStringExtra("name")
        name_text_view.text = name

        val viewManager = LinearLayoutManager(this)
        attendance =
            ViewAttendanceActivity.attendanceList
        val mAdapter = AttendanceAdapter()
        time_details_recycler_view.apply {
            layoutManager = viewManager
            adapter = mAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private inner class AttendanceAdapter : RecyclerView.Adapter<AttendanceAdapter.MyViewHolder>() {
        inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val date: TextView by lazy { view.findViewById<TextView>(R.id.date) }
            val time: TextView by lazy { view.findViewById<TextView>(R.id.time) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view: View = LayoutInflater.from(this@PersonDetailActivity)
                .inflate(R.layout.person_time_record, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int = attendance!!.size

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.date.text = attendance!![position].toLocalDate().toString()
            holder.time.text = attendance!![position].toLocalTime().toString()
        }
    }
}
