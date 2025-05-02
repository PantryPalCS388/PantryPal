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
    private var editListener: ((GroceryItem) -> Unit)? = null

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
        val expirationDateTextView = view.findViewById<TextView>(R.id.tvDetailExpirationDate)
        val deleteButton = view.findViewById<Button>(R.id.btnDeleteItem)
        val editButton = view.findViewById<Button>(R.id.btnEdit)

        // Updated formatting
        nameTextView.text = "Item: ${groceryItem.name} (${groceryItem.quantity} ${groceryItem.unit})"
        expirationDateTextView.text = "Expires: ${groceryItem.expirationDate}"

        deleteButton.setOnClickListener {
            deleteListener?.invoke()
            dismiss()
        }

        editButton.setOnClickListener {
            editListener?.invoke(groceryItem)
            dismiss()
        }
    }

    fun setDeleteListener(listener: () -> Unit) {
        deleteListener = listener
    }

    fun setEditListener(listener: (GroceryItem) -> Unit) {
        editListener = listener
    }
}
