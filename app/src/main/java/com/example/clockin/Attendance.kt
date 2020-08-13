package com.example.clockin

import java.time.LocalDateTime

class Attendance(
    var id: Int,
    var firstName: String,
    var lastName: String,
    var department: String,
    var attendance: ArrayList<LocalDateTime>
) {
    override fun toString(): String {
        return "id: $id firstName $firstName  lastName: $lastName department: $department"
    }
}