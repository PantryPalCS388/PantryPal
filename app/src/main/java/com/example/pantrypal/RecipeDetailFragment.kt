package com.example.pantrypal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RecipeDetailFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_RECIPE = "recipe"

        fun newInstance(recipe: Recipe): RecipeDetailFragment {
            val fragment = RecipeDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_RECIPE, recipe)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_recipe_detail, container, false)

        val recipe = arguments?.getSerializable(ARG_RECIPE) as? Recipe

        val titleTextView = view.findViewById<TextView>(R.id.tvRecipeDetailTitle)
        val ingredientsTextView = view.findViewById<TextView>(R.id.tvIngredients)
        val instructionsTextView = view.findViewById<TextView>(R.id.tvInstructions)

        recipe?.let {
            titleTextView.text = it.title
            ingredientsTextView.text = "Ingredients:\n" + it.ingredients.joinToString("\n")

            // Split instructions by newline and number them
            val instructionsList = it.instructions.split("\n")
            val numberedInstructions = instructionsList
                .mapIndexed { index, instruction -> "${index + 1}. $instruction" }
                .joinToString("\n")

            instructionsTextView.text = "Instructions:\n${it.instructions}"

        }

        return view
    }
}

