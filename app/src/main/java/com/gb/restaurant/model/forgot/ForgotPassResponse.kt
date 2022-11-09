package com.gb.restaurant.model.forgot


import com.google.gson.annotations.SerializedName

data class ForgotPassResponse(
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)