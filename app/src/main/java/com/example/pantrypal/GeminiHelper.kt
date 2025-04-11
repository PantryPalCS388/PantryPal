package com.example.pantrypal

import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel

object GeminiHelper {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro-001",
        apiKey = "AIzaSyCUUhunCa2ZPoJP9RQLIF8bcEK9x4L8TNo"
    )

    fun sendPrompt(activity: ComponentActivity, prompt: String, textView: TextView) {
        var output = ""

        activity.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text?.let { outputContent ->
                    output = outputContent
                }

                Log.v("Gemini output", output)

                activity.runOnUiThread {
                    textView.text = output
                }

            } catch (e: Exception) {
                Log.e("Gemini Error", e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
