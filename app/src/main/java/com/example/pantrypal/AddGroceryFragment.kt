package com.example.pantrypal

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
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
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                val topLabel = labels.maxByOrNull { it.confidence }
                if (topLabel != null) {
                    val detectedItem = topLabel.text.lowercase()
                    Toast.makeText(requireContext(), "Detected: $detectedItem", Toast.LENGTH_SHORT).show()
                    nameEditText.setText(detectedItem)
                } else {
                    Toast.makeText(requireContext(), "Couldn't detect item", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error recognizing image", Toast.LENGTH_SHORT).show()
            }
    }
}
