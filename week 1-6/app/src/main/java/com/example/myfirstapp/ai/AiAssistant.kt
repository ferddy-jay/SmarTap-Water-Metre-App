package com.example.myfirstapp.ai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AiAssistant {
    private const val API_KEY = "YOUR_API_KEY"
    
    private val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = API_KEY
        )
    }

    suspend fun getTenantTips(tenantNames: List<String>): String = withContext(Dispatchers.IO) {
        if (API_KEY == "YOUR_API_KEY") {
            return@withContext "AI Tip: Consider checking water usage for ${tenantNames.joinToString(", ")}."
        }
        
        try {
            val response = model.generateContent("Give a management tip for tenants: ${tenantNames.joinToString(", ")}")
            response.text ?: "No tips available."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
