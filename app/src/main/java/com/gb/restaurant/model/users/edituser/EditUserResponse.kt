package com.gb.restaurant.model.users.edituser


import com.google.gson.annotations.SerializedName

data class EditUserResponse(
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)