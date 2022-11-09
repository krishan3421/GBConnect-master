package com.gb.restaurant.model.order


import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("data")
    val data: List<Data?>?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("reservation")
    val reservation: Int?
)