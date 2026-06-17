package com.example.myfirstapp.network.model

data class RemoteUser(
    val id: Int,
    val name: String,
    val email: String,
    val company: Company
)

data class Company(
    val name: String,
    val catchPhrase: String
)
