package com.example.pantrypal

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.app.DatePickerDialog


class PantryFragment : Fragment(R.layout.fragment_pantry) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroceryAdapter
    private lateinit var sortSpinner: Spinner
    private lateinit var searchInput: EditText

    private val groceryList = mutableListOf<GroceryItem>()
    private val fullGroceryList = mutableListOf<GroceryItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewPantry)
        sortSpinner = view.findViewById(R.id.spinnerSort)
        searchInput = view.findViewById(R.id.etSearchPantry)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GroceryAdapter(groceryList)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { groceryItem ->
            val detailFragment = ItemDetailFragment.newInstance(groceryItem)
            detailFragment.setDeleteListener {
                deleteGroceryItem(groceryItem)
            }
            detailFragment.setEditListener {
                showEditDialog(groceryItem)
            }
            detailFragment.show(childFragmentManager, "ItemDetail")
        }

        val sortOptions = arrayOf("Alphabetical", "Expiration Date")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions)
        sortSpinner.adapter = spinnerAdapter
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> sortAlphabetically()
                    1 -> sortByExpirationDate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filteredList = fullGroceryList.filter {
                    it.name.lowercase().contains(query)
                }
                groceryList.clear()
                groceryList.addAll(filteredList)
                adapter.notifyDataSetChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fetchGroceryItems()
    }

    private fun sortAlphabetically() {
        groceryList.sortBy { it.name }
        adapter.notifyDataSetChanged()
    }

    private fun sortByExpirationDate() {
        groceryList.sortBy { it.expirationDate }
        adapter.notifyDataSetChanged()
    }


    private fun fetchGroceryItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("pantry")
                .get()
                .addOnSuccessListener { snapshot ->
                    groceryList.clear()
                    fullGroceryList.clear()

                    for (document in snapshot.documents) {
                        val name = document.getString("name") ?: ""
                        val quantity = document.getLong("quantity")?.toInt() ?: 0
                        val unit = document.getString("unit") ?: ""
                        val expirationDate = document.getString("expirationDate") ?: ""

                        val groceryItem = GroceryItem(name, quantity, unit, expirationDate, document.id)
                        groceryList.add(groceryItem)
                        fullGroceryList.add(groceryItem)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateGroceryItem(item: GroceryItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("pantry")
            .document(item.documentId)
            .set(item)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Item updated!", Toast.LENGTH_SHORT).show()
                fetchGroceryItems()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditDialog(item: GroceryItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_grocery, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etEditQuantity)
        val spUnit = dialogView.findViewById<Spinner>(R.id.spEditUnit)
        val etExpiration = dialogView.findViewById<EditText>(R.id.etEditExpiration)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Spinner setup
        val units = arrayOf("kg", "lbs", "ounces", "grams", "liters", "fluid oz")
        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spUnit.adapter = unitAdapter

        // Pre-fill values
        etName.setText(item.name)
        etQuantity.setText(item.quantity.toString())
        etExpiration.setText(item.expirationDate)

        // Set selected unit in spinner
        val unitIndex = units.indexOfFirst { it.equals(item.unit, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
        spUnit.setSelection(unitIndex)

        // Date picker on click
        etExpiration.setOnClickListener {
            val parts = item.expirationDate.split("-")
            val year = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
            val month = parts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: calendar.get(Calendar.MONTH)
            val day = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(y, m, d)
                etExpiration.setText(dateFormat.format(calendar.time))
            }, year, month, day)

            datePicker.show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Grocery Item")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedItem = item.copy(
                    name = etName.text.toString(),
                    quantity = etQuantity.text.toString().toIntOrNull() ?: 0,
                    unit = spUnit.selectedItem.toString(),
                    expirationDate = etExpiration.text.toString()
                )
                updateGroceryItem(updatedItem)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    fun addGroceryItem(groceryItem: GroceryItem) {
        groceryList.add(groceryItem)
        fullGroceryList.add(groceryItem)
        adapter.notifyDataSetChanged()
    }

    fun deleteGroceryItem(item: GroceryItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("pantry")
            .document(item.documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Item deleted!", Toast.LENGTH_SHORT).show()
                groceryList.remove(item)
                fullGroceryList.remove(item)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
