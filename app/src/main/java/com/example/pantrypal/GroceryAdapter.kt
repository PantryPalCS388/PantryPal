package com.example.pantrypal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
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
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val diffInMillis = expDate.time - today.time
                val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

                // Show notification if exactly 4 days left
                if (diffInDays < 4) {
                    showNotification(itemView.context, groceryItem.name, diffInDays)
                }

                // Update outline color dynamically
                val color = when {
                    diffInDays < 3 -> Color.RED
                    diffInDays in 3..6 -> Color.YELLOW
                    else -> Color.GREEN
                }

                cardView.setCardBackgroundColor(Color.WHITE)
                cardView.setStrokeColor(color)
                cardView.strokeWidth = 4

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GroceryAdapter", "Date parse error: ${e.message}")
            }
        }
    }

    private fun showNotification(context: Context, itemName: String, daysLeft: Int) {
        val channelId = "expiry_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Expiring Items",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Items expiring soon"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Expiring Soon")
            .setContentText("$itemName expires in $daysLeft days!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(itemName.hashCode(), notification)
    }
}
