package com.gb.restaurant.model.orderdetail


import com.gb.restaurant.model.order.Data
import com.google.gson.annotations.SerializedName

data class OrderDetailResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)