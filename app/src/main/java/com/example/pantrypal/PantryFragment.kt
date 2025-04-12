package com.example.pantrypal



import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// PantryFragment.kt
class PantryFragment : Fragment(R.layout.fragment_pantry) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroceryAdapter
    private lateinit var sortSpinner: Spinner
    private val groceryList = mutableListOf<GroceryItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewPantry)
        sortSpinner = view.findViewById(R.id.spinnerSort)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GroceryAdapter(groceryList)
        recyclerView.adapter = adapter

        // 🔥 Set the item click listener to show the detail fragment
        adapter.setOnItemClickListener { groceryItem ->
            val detailFragment = ItemDetailFragment.newInstance(groceryItem)
            detailFragment.setDeleteListener {
                deleteGroceryItem(groceryItem)
            }
            detailFragment.show(childFragmentManager, "ItemDetail")
        }

        // Spinner setup
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

        // Firestore data fetch
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
                    groceryList.clear()  // Clear the list before adding fresh data
                    for (document in snapshot.documents) {
                        val name = document.getString("name") ?: ""
                        val quantity = document.getLong("quantity")?.toInt() ?: 0
                        val unit = document.getString("unit") ?: ""
                        val expirationDate = document.getString("expirationDate") ?: ""

                        val groceryItem = GroceryItem(name, quantity, unit, expirationDate,document.id)
                        groceryList.add(groceryItem)
                    }
                    adapter.notifyDataSetChanged()  // Notify adapter of data change
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

    // Add the new grocery item to the list
    fun addGroceryItem(groceryItem: GroceryItem) {
        groceryList.add(groceryItem)
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
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
