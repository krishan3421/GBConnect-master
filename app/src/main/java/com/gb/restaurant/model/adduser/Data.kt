package com.gb.restaurant.model.adduser


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("api_id")
    val apiId: String?,
    @SerializedName("api_key")
    val apiKey: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("restaurant_id")
    val restaurantId: String?,
    @SerializedName("service_type")
    val serviceType: String?,
    @SerializedName("userid")
    val userid: String?,
    @SerializedName("userpass")
    val userpass: String?
)