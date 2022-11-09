package com.gb.restaurant.model.support


import com.google.gson.annotations.SerializedName

data class SupportResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)