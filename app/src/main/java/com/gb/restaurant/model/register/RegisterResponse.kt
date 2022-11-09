package com.gb.restaurant.model.register


import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)