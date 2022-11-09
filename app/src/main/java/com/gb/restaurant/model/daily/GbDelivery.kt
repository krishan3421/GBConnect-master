package com.gb.restaurant.model.daily


import com.google.gson.annotations.SerializedName

data class GbDelivery(
    @SerializedName("heading")
    val heading: String?,
    @SerializedName("summary")
    val summary: List<Summary>?
)