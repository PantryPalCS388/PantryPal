package com.example.pantrypal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipesFragment : Fragment() {

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var searchInput: EditText
    private val recipeList = listOf(
        Recipe("Spaghetti Bolognese", listOf("Pasta", "Ground beef", "Tomato sauce"), "Cook pasta. \nBrown beef. \nMix with sauce."),
        Recipe("Pancakes", listOf("Flour", "Eggs", "Milk", "Butter"), "Mix ingredients. \nCook on skillet."),
        Recipe("Omelette", listOf("Eggs", "Cheese", "Spinach"), "Whisk eggs. \nAdd fillings. \nCook on pan."),
    )
    private val displayedRecipes = recipeList.toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipes, container, false)

        searchInput = view.findViewById(R.id.etSearch)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvRecipes)

        recipeAdapter = RecipeAdapter(displayedRecipes) { recipe ->
            val detailFragment = RecipeDetailFragment.newInstance(recipe)
            detailFragment.show(parentFragmentManager, "recipeDetail")
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = recipeAdapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                displayedRecipes.clear()
                displayedRecipes.addAll(recipeList.filter {
                    it.title.lowercase().contains(query)
                })
                recipeAdapter.notifyDataSetChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        getPantryItemsAsList { pantryList ->
            Log.d("PantryData", pantryList.toString())
        }

        return view
    }

    private fun getPantryItemsAsList(callback: (List<List<Any>>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("pantry")
                .get()
                .addOnSuccessListener { snapshot ->
                    val result = mutableListOf<List<Any>>()
                    for (document in snapshot.documents) {
                        val name = document.getString("name") ?: ""
                        val quantity = document.getLong("quantity")?.toInt() ?: 0
                        val expirationDate = document.getString("expirationDate") ?: ""

                        result.add(listOf(name, quantity, expirationDate))
                    }
                    callback(result)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error fetching pantry: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
        } else {
            callback(emptyList())
        }
    }
}
