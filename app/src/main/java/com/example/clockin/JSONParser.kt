package com.example.clockin

import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime

private const val TAG = "JSONParser"

class JSONParser(private val listener: OnDataAvailable) :
    AsyncTask<String, Void, ArrayList<Attendance>>() {

    interface OnDataAvailable {
        fun onDataAvailable(data: ArrayList<Attendance>)
        fun onError(exception: Exception)
    }

    override fun doInBackground(vararg params: String?): ArrayList<Attendance> {
        val attendanceList = ArrayList<Attendance>()

        try {
            val jsonData = JSONObject(params[0]!!)
            val data = jsonData.getJSONArray("data")

            for (i in 0 until data.length()) {
                val jsonAttendance = data.getJSONObject(i)
                val id = jsonAttendance.getInt("id")
                val firstName = jsonAttendance.getString("firstName")
                val lastName = jsonAttendance.getString("lastName")
                val department = jsonAttendance.getString("department")
                val attendanceData = jsonAttendance.getJSONArray("attendance")

                val attendance = Attendance(id, firstName, lastName, department, ArrayList())

                for (j in 0 until attendanceData.length()) {
                    val year = attendanceData.getJSONObject(j).getInt("year")
                    val month = attendanceData.getJSONObject(j).getInt("month")
                    val day = attendanceData.getJSONObject(j).getInt("day")
                    val hour = attendanceData.getJSONObject(j).getInt("hour")
                    val minute = attendanceData.getJSONObject(j).getInt("minute")
                    val localDateTime = LocalDateTime.of(year, month, day, hour, minute)
                    attendance.attendance.add(localDateTime)
                }

                attendanceList.add(attendance)
            }
        } catch (e: JSONException) {
            Log.d(TAG, ".doInBackground has hit an exception")
            e.printStackTrace()
            cancel(true)
            listener.onError(e)
        }

        return attendanceList
    }

    override fun onPostExecute(result: ArrayList<Attendance>) {
        listener.onDataAvailable(result)
    }
}