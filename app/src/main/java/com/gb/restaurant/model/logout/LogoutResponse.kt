package com.gb.restaurant.model.logout


import com.google.gson.annotations.SerializedName

data class LogoutResponse(
    @SerializedName("data")
    val `data`: Any?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)