package com.gb.restaurant.model.confirmorder


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("id")
    val id: String?,
    @SerializedName("status")
    val status: String?
)