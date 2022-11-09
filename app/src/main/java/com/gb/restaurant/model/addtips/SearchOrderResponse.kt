package com.gb.restaurant.model.addtips


import com.google.gson.annotations.SerializedName

data class SearchOrderResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)