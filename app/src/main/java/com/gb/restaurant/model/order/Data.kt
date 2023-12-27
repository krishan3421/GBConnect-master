package com.gb.restaurant.model.order


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("date")
    val date: String?,
    @SerializedName("date2")
    val date2: String?,
    @SerializedName("delivery")
    val delivery: String?,
    @SerializedName("deliverycharge")
    val deliverycharge: String?,
    @SerializedName("details")
    val details: String?,
    @SerializedName("hold")
    val hold: String?,
    @SerializedName("holddate")
    val holddate: String?,
    @SerializedName("holddate2")
    val holddate2: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("items")
    val items: List<Item?>?,
    @SerializedName("mobile")
    val mobile: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("offer")
    val offer: String?,
    @SerializedName("offeramount")
    val offeramount: String?,
    @SerializedName("orderat")
    val orderat: String?,
    @SerializedName("orderid")
    val orderid: String?,
    @SerializedName("payment")
    var payment: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("subtotal")
    val subtotal: Double?,
    @SerializedName("tax")
    val tax: Double?,
    @SerializedName("tip")
    val tip: String?,
    @SerializedName("tip2")
    val tip2: String?,
    @SerializedName("total")
    val total: Double?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("payby")
    var payby: String?,
    @SerializedName("rewards")
    var rewards: String?

)