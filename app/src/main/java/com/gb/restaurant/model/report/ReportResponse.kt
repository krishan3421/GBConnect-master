package com.gb.restaurant.model.report


import com.google.gson.annotations.SerializedName

data class ReportResponse(
    @SerializedName("data")
    val `data`: List<Data?>?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("amount")
    val amount: String?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("invoice")
    val invoice: String?,
    @SerializedName("payment")
    val payment: String?
)