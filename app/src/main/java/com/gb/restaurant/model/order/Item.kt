package com.gb.restaurant.model.order


import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("extra")
    val extra: String?="",
    @SerializedName("heading")
    var heading: String?="",
    @SerializedName("instruction")
    val instruction: String?="",
    @SerializedName("price")
    var price: Double?=0.0,
    @SerializedName("qty")
    val qty: String?="",
    @SerializedName("isItem")
    var isItem: Boolean = true,
    @SerializedName("isDividerLine")
    var isDividerLine: Boolean = true,
    @SerializedName("isOffer")
    var isOffer: Boolean = false,
    @SerializedName("isBold")
    var isBold: Boolean = false
)