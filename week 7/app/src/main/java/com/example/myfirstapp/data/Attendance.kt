package com.example.myfirstapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an attendance record for a tenant.
 * Task 3, Week 7: Manage tenant attendance.
 */
@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenantId: Int,
    val tenantName: String,
    val date: String,
    val isPresent: Boolean
)
