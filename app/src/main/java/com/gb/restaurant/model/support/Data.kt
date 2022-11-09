package com.gb.restaurant.model.support


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("message")
    val message: String?
)