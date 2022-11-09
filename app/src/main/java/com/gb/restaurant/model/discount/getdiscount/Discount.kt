package com.gb.restaurant.model.discount.getdiscount


import com.google.gson.annotations.SerializedName

data class Discount(
    @SerializedName("details")
    val details: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("minorder")
    val minorder: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("types")
    val types: String?
)