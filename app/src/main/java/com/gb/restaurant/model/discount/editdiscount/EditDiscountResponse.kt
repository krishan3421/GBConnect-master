package com.gb.restaurant.model.discount.editdiscount


import com.google.gson.annotations.SerializedName

data class EditDiscountResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)