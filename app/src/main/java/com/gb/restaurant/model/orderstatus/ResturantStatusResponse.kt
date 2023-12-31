package com.gb.restaurant.model.orderstatus


import com.google.gson.annotations.SerializedName

data class ResturantStatusResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)