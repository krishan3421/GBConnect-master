package com.gb.restaurant.model.resetpass


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("password")
    val password: String?
)