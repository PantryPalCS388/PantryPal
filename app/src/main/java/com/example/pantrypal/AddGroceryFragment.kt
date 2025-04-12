package com.example.pantrypal
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.app.DatePickerDialog

class AddGroceryFragment : Fragment(R.layout.fragment_add_grocery) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameEditText = view.findViewById<EditText>(R.id.etName)
        val quantityEditText = view.findViewById<EditText>(R.id.etQuantity)
        val unitSpinner = view.findViewById<Spinner>(R.id.spUnit)
        val expirationDateEditText = view.findViewById<EditText>(R.id.etExpirationDate)
        val addButton = view.findViewById<Button>(R.id.btnAddGrocery)

        val units = arrayOf("kg", "lbs", "ounces", "grams", "liters", "fluid oz")
        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = unitAdapter

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
                // Get the current user ID
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null) {
                    val newGroceryItem = hashMapOf(
                        "name" to name,
                        "quantity" to quantity.toInt(),
                        "unit" to unit,
                        "expirationDate" to expirationDate
                    )

                    // Add the new grocery item to Firestore under the current user's UID
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)  // Reference the specific user by UID
                        .collection("pantry")  // Subcollection for groceries
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
    }
}
