package com.gb.restaurant.model.bank


import com.google.gson.annotations.SerializedName

data class GetBankDetailResponse(
    @SerializedName("data")
    val data:Data,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)