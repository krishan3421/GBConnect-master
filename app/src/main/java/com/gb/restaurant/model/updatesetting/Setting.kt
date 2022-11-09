package com.gb.restaurant.model.updatesetting


import com.google.gson.annotations.SerializedName

data class Setting(
    @SerializedName("dchargetype")
    val dchargetype: String?,
    @SerializedName("delivery")
    val delivery: String?,
    @SerializedName("deliverycharge")
    val deliverycharge: String?,
    @SerializedName("miles")
    val miles: String?,
    @SerializedName("mindelivery")
    val mindelivery: String?,
    @SerializedName("pickup")
    val pickup: String?
)