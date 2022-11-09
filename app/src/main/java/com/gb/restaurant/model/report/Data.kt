package com.gb.restaurant.model.report


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("key")
    val key: String?,
    @SerializedName("value")
    val value: String?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("payment")
    val payment: String?,
    @SerializedName("amount")
    val amount: String?,
    @SerializedName("invoice")
    val invoice: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("orderurl")
    val orderurl: String?
)