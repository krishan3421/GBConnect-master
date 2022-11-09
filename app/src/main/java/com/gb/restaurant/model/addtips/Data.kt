package com.gb.restaurant.model.addtips


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("date")
    val date: String?,
    @SerializedName("date2")
    val date2: String?,
    @SerializedName("delivery")
    val delivery: String?,
    @SerializedName("hold")
    val hold: String?,
    @SerializedName("holddate")
    val holddate: Any?,
    @SerializedName("holddate2")
    val holddate2: Any?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("landmark")
    val landmark: String?,
    @SerializedName("mobile")
    val mobile: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("orderat")
    val orderat: String?,
    @SerializedName("orderid")
    val orderid: String?,
    @SerializedName("payment")
    val payment: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("tips2")
    val tips2: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("payby")
    val payby: String?

)