package com.example.pantrypal

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object GeminiHelper {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro-001",
        apiKey = "input api key"
    )

    fun sendPrompt(activity: ComponentActivity, prompt: String, onResult: (String) -> Unit) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                val output = response.text ?: ""

                activity.runOnUiThread {
                    onResult(output)
                }

            } catch (e: Exception) {
                Log.e("Gemini Error", e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
