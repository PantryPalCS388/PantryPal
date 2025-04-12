package com.example.pantrypal

import android.os.Parcel
import android.os.Parcelable
data class GroceryItem(
    val name: String = "",
    val quantity: Int = 0,
    val unit: String = "",
    val expirationDate: String = "",
    val documentId: String = "" // <-- new field
) : Parcelable {
    // don't forget to add this to parcelable logic too
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(quantity)
        parcel.writeString(unit)
        parcel.writeString(expirationDate)
        parcel.writeString(documentId) // <-- new line
    }
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GroceryItem> {
        override fun createFromParcel(parcel: Parcel): GroceryItem {
            return GroceryItem(parcel)
        }
        override fun newArray(size: Int): Array<GroceryItem?> {
            return arrayOfNulls(size)
        }
    }
}

