package com.gb.restaurant.model.discount.adddiscount


import com.google.gson.annotations.SerializedName

data class AddDiscountResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)