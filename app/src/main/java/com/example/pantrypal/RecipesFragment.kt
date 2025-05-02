package com.example.pantrypal

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

class RecipesFragment : Fragment() {

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private val displayedRecipes = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipes, container, false)

        val btnFind = view.findViewById<Button>(R.id.btnFindRecipes)
        searchInput = view.findViewById(R.id.etSearch)
        recyclerView = view.findViewById(R.id.rvRecipes)

        recipeAdapter = RecipeAdapter(displayedRecipes) { recipe ->
            val detailFragment = RecipeDetailFragment.newInstance(recipe)
            detailFragment.show(parentFragmentManager, "recipeDetail")
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = recipeAdapter

        // Filter logic for search bar
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().lowercase()
                val filtered = displayedRecipes.filter {
                    it.title.lowercase().contains(query)
                }

                recipeAdapter = RecipeAdapter(filtered) { recipe ->
                    val detailFragment = RecipeDetailFragment.newInstance(recipe)
                    detailFragment.show(parentFragmentManager, "recipeDetail")
                }
                recyclerView.adapter = recipeAdapter
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Button click triggers AI recipe generation
        btnFind.setOnClickListener {
            getPantryItemsAndFetchRecipes()
        }

        return view
    }

    private fun getPantryItemsAndFetchRecipes() {
        Log.d("ButtonClick", "Find Recipes clicked")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("pantry")
                .get()
                .addOnSuccessListener { snapshot ->
                    val ingredients = snapshot.documents.mapNotNull { it.getString("name") }

                    val prompt = """
                        Based on these ingredients: ${ingredients.joinToString(", ")}, suggest 3 creative recipes.

                        Format your response in the following JSON structure exactly:

                        {
                          "names": ["Recipe 1", "Recipe 2", "Recipe 3"],
                          "ingredients": [
                            ["1 cup ingredient 1", "3 tbsp ingredient 2"],
                            ["8 ounces ingredient 1", "3 cups ingredient 2"],
                            ["4 tsp ingredient 1", "1/2 cup ingredient 2"]
                          ],
                          "instructions": [
                            ["instruction 1", "instruction 2"],
                            ["instruction 1", "instruction 2"],
                            ["instruction 1", "instruction 2"]
                          ]
                        }

                        Do not include anything else outside of this JSON structure. Try to split long instructions into multiple.
                    """.trimIndent()

                    activity?.let { act ->
                        GeminiHelper.sendPrompt(act, prompt) { resultJson ->
                            try {
                                val recipes = parseRecipesFromJson(resultJson)
                                displayedRecipes.clear()
                                displayedRecipes.addAll(recipes)
                                recipeAdapter.notifyDataSetChanged()
                            } catch (e: Exception) {
                                Log.e("ParseError", "Failed to parse: ${e.message}")
                                Toast.makeText(requireContext(), "Error parsing recipes", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error fetching pantry: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun parseRecipesFromJson(json: String): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val jsonObject = JSONObject(json)

        val namesArray = jsonObject.getJSONArray("names")
        val ingredientsArray = jsonObject.getJSONArray("ingredients")
        val instructionsArray = jsonObject.getJSONArray("instructions")

        for (i in 0 until namesArray.length()) {
            val name = namesArray.getString(i)

            val ingredients = mutableListOf<String>()
            val ingArray = ingredientsArray.getJSONArray(i)
            for (j in 0 until ingArray.length()) {
                ingredients.add(ingArray.getString(j))
            }

            val instructions = StringBuilder()
            val instrArray = instructionsArray.getJSONArray(i)
            for (j in 0 until instrArray.length()) {
                instructions.append("${j + 1}. ${instrArray.getString(j)}\n")
            }

            recipes.add(Recipe(name, ingredients, instructions.toString().trim()))
        }

        return recipes
    }
}
