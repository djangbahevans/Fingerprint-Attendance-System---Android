package com.example.clockin.ui.viewattendance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clockin.*
import com.example.clockin.ui.persondetail.PersonDetailActivity
import kotlinx.android.synthetic.main.activity_view_attendance.*
import java.time.LocalDateTime

private const val TAG = "ViewAttendanceActivity"

class ViewAttendanceActivity : AppCompatActivity(),
    JSONParser.OnDataAvailable, DownloadData.OnDownloadComplete {

    private var list: ArrayList<Attendance> = ArrayList()
    private val mAdapter: AttendanceAdapter by lazy { AttendanceAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_attendance)

        val viewManager = LinearLayoutManager(this)
        attendance_recycler_view.apply {
            layoutManager = viewManager
            adapter = mAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        swipe_refresh.setOnRefreshListener {
            DownloadData(this).execute(Constants.URL)
        }

        DownloadData(this).execute(Constants.URL)
    }

    private inner class AttendanceAdapter :
        RecyclerView.Adapter<AttendanceAdapter.MyViewHolder>() {

        inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {

            val name: TextView by lazy { view.findViewById<TextView>(R.id.person_name) }
            val lastLog: TextView by lazy { view.findViewById<TextView>(R.id.last_record) }
            val department: TextView by lazy { view.findViewById<TextView>(R.id.person_department) }
            var attendance: ArrayList<LocalDateTime>? = null

            init {
                view.isClickable = true
                view.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                val intent =
                    Intent(this@ViewAttendanceActivity, PersonDetailActivity::class.java).apply {
                        putExtra("name", name.text)
//                    putExtra("attendance", attendance) TODO: // Find better way to send attendance dates
                    }
                attendanceList = attendance
                startActivity(intent)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view: View = LayoutInflater.from(this@ViewAttendanceActivity)
                .inflate(R.layout.person_record, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val attendance = list[position].attendance
            if (attendance.size > 0) {
                val lastLogDate = attendance[attendance.size - 1].toLocalDate().toString()
                val lastLogTime = attendance[attendance.size - 1].toLocalTime().toString()
                holder.lastLog.text =
                    getString(R.string.time_display).format(lastLogDate, lastLogTime)
            }
            holder.name.text = getString(R.string.name_display).format(
                list[position].firstName,
                list[position].lastName
            )
            holder.department.text = list[position].department
            holder.attendance = attendance
        }
    }

    companion object {
        var attendanceList: ArrayList<LocalDateTime>? = null
    }

    override fun onDataAvailable(data: ArrayList<Attendance>) {
        Log.d(TAG, ".onDataAvailable: called")
        list = data
        mAdapter.notifyDataSetChanged()
        swipe_refresh.isRefreshing = false
    }

    override fun onError(exception: Exception) {
        throw exception
    }

    override fun onDownloadComplete(data: String, status: DownloadData.DownloadStatus) {
        Log.d(TAG, ".onDownloadComplete: data $data")
        JSONParser(this).execute(data)
    }
}
