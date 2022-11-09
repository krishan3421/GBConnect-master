package com.gb.restaurant.model.additem


import com.google.gson.annotations.SerializedName

data class AddOrderItemResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)