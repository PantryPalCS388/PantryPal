package com.example.pantrypal

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

class PantryFragment : Fragment(R.layout.fragment_pantry) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroceryAdapter
    private lateinit var sortSpinner: Spinner
    private lateinit var searchInput: EditText

    private val groceryList = mutableListOf<GroceryItem>()
    private val fullGroceryList = mutableListOf<GroceryItem>() // unfiltered full copy

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewPantry)
        sortSpinner = view.findViewById(R.id.spinnerSort)
        searchInput = view.findViewById(R.id.etSearchPantry) // 🛠️ bind the EditText

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GroceryAdapter(groceryList)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { groceryItem ->
            val detailFragment = ItemDetailFragment.newInstance(groceryItem)
            detailFragment.setDeleteListener {
                deleteGroceryItem(groceryItem)
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

        // 🔥 Setup search input
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

    private fun sortAlphabetically() {
        groceryList.sortBy { it.name }
        adapter.notifyDataSetChanged()
    }

    private fun sortByExpirationDate() {
        groceryList.sortBy { it.expirationDate }
        adapter.notifyDataSetChanged()
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
