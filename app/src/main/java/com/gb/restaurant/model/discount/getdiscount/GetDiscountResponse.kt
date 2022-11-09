package com.gb.restaurant.model.discount.getdiscount


import com.gb.restaurant.model.discount.getdiscount.Discount
import com.google.gson.annotations.SerializedName

data class GetDiscountResponse(
    @SerializedName("data")
    val data: List<Discount>?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("offers")
    val offers: Int?,
    @SerializedName("month")
    val month: String?,
    @SerializedName("users")
    val users: Int?,
    @SerializedName("status")
    val status: String?
)