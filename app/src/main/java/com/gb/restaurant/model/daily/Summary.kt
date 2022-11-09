package com.gb.restaurant.model.daily


import com.google.gson.annotations.SerializedName

data class Summary(
    @SerializedName("key")
    val key: String?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("value")
    val value: String?
)