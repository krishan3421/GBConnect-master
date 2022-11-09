package com.gb.restaurant.model.updatesetting


import com.google.gson.annotations.SerializedName

data class UpdateSettingResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)