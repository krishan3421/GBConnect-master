package com.gb.restaurant.model.users.rmuser


import com.google.gson.annotations.SerializedName

data class RmUserResponse(
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)