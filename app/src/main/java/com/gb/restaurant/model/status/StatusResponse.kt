package com.gb.restaurant.model.status


import com.google.gson.annotations.SerializedName

data class StatusResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)