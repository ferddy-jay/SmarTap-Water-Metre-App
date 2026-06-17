package com.example.myfirstapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants")
    fun getAllTenants(): Flow<List<Tenant>>

    @Query("SELECT * FROM tenants")
    suspend fun getAllTenantsSync(): List<Tenant>

    @Insert
    suspend fun insertTenant(tenant: Tenant)

    @Delete
    suspend fun deleteTenant(tenant: Tenant)

    @Query("DELETE FROM tenants")
    suspend fun deleteAllTenants()
}
