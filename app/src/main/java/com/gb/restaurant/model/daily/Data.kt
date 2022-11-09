package com.gb.restaurant.model.daily


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("cash")
    val cash: String?,
    @SerializedName("gbDelivery")
    val gbDelivery: GbDelivery?,
    @SerializedName("orders")
    val orders: Int?,
    @SerializedName("prepaid")
    val prepaid: String?,
    @SerializedName("summaryList")
    val summaryList: List<Summary>?
)