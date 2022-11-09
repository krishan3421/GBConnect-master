package com.gb.restaurant.model.adduser


import com.google.gson.annotations.SerializedName

data class AddUserResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)