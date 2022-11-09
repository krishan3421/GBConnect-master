package com.gb.restaurant.model.addordertips


import com.google.gson.annotations.SerializedName

data class OrderTipsResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)