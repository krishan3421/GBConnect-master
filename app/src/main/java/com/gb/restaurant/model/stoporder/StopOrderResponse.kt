package com.gb.restaurant.model.stoporder


import com.google.gson.annotations.SerializedName

data class StopOrderResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)