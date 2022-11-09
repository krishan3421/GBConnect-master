package com.gb.restaurant.model.reservation


import com.google.gson.annotations.SerializedName

data class ReservationResponse(
    @SerializedName("data")
    val `data`: List<Data?>?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)