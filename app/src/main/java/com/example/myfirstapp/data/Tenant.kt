package com.example.myfirstapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val metreNumber: String,
    val apartmentName: String,
    val waterCompany: String
)
