package com.example.pantrypal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroceryAdapter(private val groceryList: List<GroceryItem>) : RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder>() {

    private var itemClickListener: ((GroceryItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (GroceryItem) -> Unit) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grocery, parent, false)
        return GroceryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        val groceryItem = groceryList[position]
        holder.bind(groceryItem)
        holder.itemView.setOnClickListener {
            itemClickListener?.invoke(groceryItem) // Notify the fragment about the item click
        }
    }

    override fun getItemCount(): Int {
        return groceryList.size
    }

    inner class GroceryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvGroceryName)
        private val expirationDateTextView: TextView = itemView.findViewById(R.id.tvExpirationDate)

        fun bind(groceryItem: GroceryItem) {
            nameTextView.text = groceryItem.name
            expirationDateTextView.text = groceryItem.expirationDate
        }
    }
}
