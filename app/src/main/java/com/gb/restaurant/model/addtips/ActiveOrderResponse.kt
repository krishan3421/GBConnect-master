package com.gb.restaurant.model.addtips


import com.google.gson.annotations.SerializedName

data class ActiveOrderResponse(
    @SerializedName("data")
    val `data`: List<Data>?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)