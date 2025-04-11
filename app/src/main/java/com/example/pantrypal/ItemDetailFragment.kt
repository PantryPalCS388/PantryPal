package com.example.pantrypal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ItemDetailFragment : DialogFragment() {

    private lateinit var groceryItem: GroceryItem
    private var deleteListener: (() -> Unit)? = null

    companion object {
        fun newInstance(groceryItem: GroceryItem): ItemDetailFragment {
            val fragment = ItemDetailFragment()
            val args = Bundle()
            args.putParcelable("groceryItem", groceryItem)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groceryItem = arguments?.getParcelable("groceryItem")!!

        val nameTextView = view.findViewById<TextView>(R.id.tvDetailName)
        val quantityTextView = view.findViewById<TextView>(R.id.tvDetailQuantity)
        val expirationDateTextView = view.findViewById<TextView>(R.id.tvDetailExpirationDate)
        val deleteButton = view.findViewById<Button>(R.id.btnDeleteItem)

        nameTextView.text = groceryItem.name
        quantityTextView.text = "${groceryItem.quantity} ${groceryItem.unit}"
        expirationDateTextView.text = groceryItem.expirationDate

        deleteButton.setOnClickListener {
            deleteListener?.invoke() // Trigger the delete action
            dismiss() // Close the dialog
        }
    }

    fun setDeleteListener(listener: () -> Unit) {
        deleteListener = listener
    }
}
