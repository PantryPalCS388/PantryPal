package com.example.pantrypal

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddGroceryFragment : Fragment(R.layout.fragment_add_grocery) {

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private lateinit var nameEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var unitSpinner: Spinner
    private lateinit var expirationDateEditText: EditText
    private lateinit var addButton: Button
    private lateinit var takePhotoButton: Button

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            recognizeItemFromImage(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameEditText = view.findViewById(R.id.etName)
        quantityEditText = view.findViewById(R.id.etQuantity)
        unitSpinner = view.findViewById(R.id.spUnit)
        expirationDateEditText = view.findViewById(R.id.etExpirationDate)
        addButton = view.findViewById(R.id.btnAddGrocery)
        takePhotoButton = view.findViewById(R.id.btnTakePhoto)

        val units = arrayOf("kg", "lbs", "ounces", "grams", "liters", "fluid oz")
        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = unitAdapter

        expirationDateEditText.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    expirationDateEditText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        addButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val quantity = quantityEditText.text.toString()
            val unit = unitSpinner.selectedItem.toString()
            val expirationDate = expirationDateEditText.text.toString()

            if (name.isEmpty() || quantity.isEmpty() || expirationDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val newGroceryItem = hashMapOf(
                        "name" to name,
                        "quantity" to quantity.toInt(),
                        "unit" to unit,
                        "expirationDate" to expirationDate
                    )

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .collection("pantry")
                        .add(newGroceryItem)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Grocery Added!", Toast.LENGTH_SHORT).show()
                            nameEditText.text.clear()
                            quantityEditText.text.clear()
                            expirationDateEditText.text.clear()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                }
            }
        }

        takePhotoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    101
                )
            } else {
                takePictureLauncher.launch(null)
            }
        }
    }

    private fun recognizeItemFromImage(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Convert Bitmap to base64
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val imageBytes = outputStream.toByteArray()
                val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)

                // 2. Build the correct JSON body manually
                val promptText = "Identify the grocery item in the image and, if possible, its expiration date. Return the response strictly in JSON format: {\"name\": \"Item\", \"expiration\": \"YYYY-MM-DD\" or \"Unknown\"}."

                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", promptText))
                partsArray.put(JSONObject().put("inline_data", JSONObject()
                    .put("mime_type", "image/jpeg")
                    .put("data", base64Image)
                ))

                val userContent = JSONObject()
                    .put("role", "user")
                    .put("parts", partsArray)

                val contentsArray = JSONArray()
                contentsArray.put(userContent)

                val requestBodyJson = JSONObject()
                    .put("contents", contentsArray)

                val body = requestBodyJson.toString().toRequestBody("application/json".toMediaType())

                // 3. Send HTTP request
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-001:generateContent?key=YOUR_API_KEY_HERE")
                    .post(body)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                val responseString = response.body?.string()

                if (response.isSuccessful && responseString != null) {
                    val jsonResponse = JSONObject(responseString)
                    val textResponse = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    try {
                        val parsedJson = JSONObject(textResponse.trim())
                        val detectedName = parsedJson.getString("name")
                        val detectedExpiration = parsedJson.getString("expiration")

                        withContext(Dispatchers.Main) {
                            nameEditText.setText(detectedName)
                            expirationDateEditText.setText(
                                if (detectedExpiration != "Unknown") detectedExpiration else ""
                            )
                            Toast.makeText(requireContext(), "Detected: $detectedName", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Failed to parse Gemini JSON response.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Even on error, try to log the body
                    android.util.Log.e("GeminiError", "HTTP ${response.code}: $responseString")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Gemini API error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
