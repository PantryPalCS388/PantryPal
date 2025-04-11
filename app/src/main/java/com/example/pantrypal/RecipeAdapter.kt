package com.example.pantrypal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeAdapter(
    private val recipes: List<Recipe>,
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeTitle: TextView = itemView.findViewById(R.id.tvRecipeTitle)

        fun bind(recipe: Recipe) {
            recipeTitle.text = recipe.title
            itemView.setOnClickListener {
                itemView.animate().alpha(0.5f).setDuration(100).withEndAction {
                    itemView.alpha = 1f
                    onClick(recipe)
                }.start()
            }

            // Animation on bind (fade in)
            itemView.alpha = 0f
            itemView.animate().alpha(1f).setDuration(500).start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size
}
