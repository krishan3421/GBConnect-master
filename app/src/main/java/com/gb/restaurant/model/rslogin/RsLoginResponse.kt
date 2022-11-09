package com.gb.restaurant.model.rslogin


import com.google.gson.annotations.SerializedName

data class RsLoginResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)