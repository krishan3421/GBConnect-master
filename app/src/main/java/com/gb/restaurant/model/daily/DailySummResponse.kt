package com.gb.restaurant.model.daily


import com.google.gson.annotations.SerializedName

data class DailySummResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)