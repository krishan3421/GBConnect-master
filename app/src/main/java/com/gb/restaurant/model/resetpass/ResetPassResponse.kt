package com.gb.restaurant.model.resetpass


import com.google.gson.annotations.SerializedName

data class ResetPassResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)