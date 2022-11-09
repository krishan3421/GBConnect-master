package com.gb.restaurant.model.reservation


import com.google.gson.annotations.SerializedName

data class StopReservationResponse(
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)