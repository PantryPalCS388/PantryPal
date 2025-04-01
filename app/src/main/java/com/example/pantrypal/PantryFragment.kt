package com.example.pantrypal

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

        adapter.setOnItemClickListener { groceryItem ->
            val itemDetailFragment = ItemDetailFragment.newInstance(groceryItem)
            itemDetailFragment.setDeleteListener {
                deleteItem(groceryItem) // Delete the item when the delete button is pressed
            }
            itemDetailFragment.show(childFragmentManager, "itemDetail")
        }

        // Set up sorting
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
    }

    fun addGroceryItem(item: GroceryItem) {
        groceryList.add(item)
        adapter.notifyDataSetChanged()
    }

    private fun deleteItem(groceryItem: GroceryItem) {
        groceryList.remove(groceryItem) // Remove the item from the list
        adapter.notifyDataSetChanged() // Notify the adapter to update the list
    }

    private fun sortAlphabetically() {
        groceryList.sortBy { it.name }
        adapter.notifyDataSetChanged()
    }

    private fun sortByExpirationDate() {
        groceryList.sortBy { it.expirationDate }
        adapter.notifyDataSetChanged()
    }
}
