package com.example.myfirstapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a Tenant in the system.
 * Adapted from the "Student Database Design" task (Task 2, Week 7).
 * Fields: ID, Name, Apartment/Unit (Course), Lease Year (Year), Phone Number.
 */
@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val apartmentName: String, // Equivalent to Course
    val leaseYear: String = "", // Equivalent to Year
    val phoneNumber: String = "", // Equivalent to Phone Number
    val metreNumber: String = "",
    val waterCompany: String = ""
)
