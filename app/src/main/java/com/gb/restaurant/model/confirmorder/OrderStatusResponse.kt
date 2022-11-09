package com.gb.restaurant.model.confirmorder


import com.google.gson.annotations.SerializedName

data class OrderStatusResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)