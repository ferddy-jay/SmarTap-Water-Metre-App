package com.example.myfirstapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}
