package com.example.pantrypal

import android.os.Parcel
import android.os.Parcelable

data class GroceryItem(
    val name: String,
    val quantity: Int,
    val unit: String,
    val expirationDate: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(quantity)
        parcel.writeString(unit)
        parcel.writeString(expirationDate)
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
