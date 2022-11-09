package com.gb.restaurant.model.discount.editdiscount


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("api_id")
    val apiId: String?,
    @SerializedName("api_key")
    val apiKey: String?,
    @SerializedName("discountid")
    val discountid: String?,
    @SerializedName("discounts")
    val discounts: String?,
    @SerializedName("discounttype")
    val discounttype: String?,
    @SerializedName("minamount")
    val minamount: String?,
    @SerializedName("restaurant_id")
    val restaurantId: String?,
    @SerializedName("service_type")
    val serviceType: String?
)