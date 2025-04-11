package com.example.pantrypal

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

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
            itemClickListener?.invoke(groceryItem)
        }
    }

    override fun getItemCount(): Int {
        return groceryList.size
    }

    inner class GroceryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvGroceryName)
        private val expirationDateTextView: TextView = itemView.findViewById(R.id.tvExpirationDate)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)

        fun bind(groceryItem: GroceryItem) {
            nameTextView.text = groceryItem.name
            expirationDateTextView.text = groceryItem.expirationDate

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            try {
                val expDate = sdf.parse(groceryItem.expirationDate)
                val today = Calendar.getInstance().time

                val diffInMillis = expDate.time - today.time
                val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

                val color = when {
                    diffInDays < 3 -> Color.RED
                    diffInDays in 3..6 -> Color.YELLOW
                    else -> Color.GREEN
                }

                cardView.setCardBackgroundColor(Color.WHITE) // background remains white
                cardView.setStrokeColor(color) // outline the card
                cardView.strokeWidth = 4 // stroke width in pixels

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
