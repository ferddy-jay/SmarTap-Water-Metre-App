package com.example.myfirstapp.network

import com.example.myfirstapp.data.Tenant
import com.example.myfirstapp.network.model.RemoteUser
import com.example.myfirstapp.network.model.Company
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import kotlinx.coroutines.delay

interface ApiService {
    @POST("sync/tenants")
    suspend fun syncTenants(@Body tenants: List<Tenant>): Response<Unit>

    /**
     * Fetches a list of users.
     * Note: In a real app, this calls a REST API. 
     * Here, we provide specific English names as requested.
     */
    @GET("users")
    suspend fun getUsers(): List<RemoteUser>

    companion object {
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            val api = retrofit.create(ApiService::class.java)

            // We create a wrapper to inject our specific names and a longer delay
            return object : ApiService {
                override suspend fun syncTenants(tenants: List<Tenant>) = api.syncTenants(tenants)
                
                override suspend fun getUsers(): List<RemoteUser> {
                    // Artificial delay of 5 seconds for screenshot purposes
                    delay(5000) 
                    
                    // Return the specific names requested
                    return listOf(
                        RemoteUser(1, "Ferdinant", "ferdinant@example.com", Company("SmarTap Solutions", "Streamlining management")),
                        RemoteUser(2, "Jessica", "jessica@example.com", Company("Creative Flow", "Designing the future")),
                        RemoteUser(3, "Flavian", "flavian@example.com", Company("Tech Pioneers", "Innovating connectivity")),
                        RemoteUser(4, "Kerry", "kerry@example.com", Company("Global Reach", "Connecting worlds")),
                        RemoteUser(5, "Keighly", "keighly@example.com", Company("Bright Ideas", "Lighting the path"))
                    )
                }
            }
        }
    }
}
