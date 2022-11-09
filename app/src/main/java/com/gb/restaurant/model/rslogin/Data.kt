package com.gb.restaurant.model.rslogin


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("dcharge")
    var dcharge: String?,
    @SerializedName("dchargetype")
    var dchargetype: String?,
    @SerializedName("delivery")
    var delivery: String?,
    @SerializedName("miles")
    var miles: String?,
    @SerializedName("mindelivery")
    var mindelivery: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("pickup")
    var pickup: String?,
    @SerializedName("restaurant_id")
    val restaurantId: String?,
    @SerializedName("stoptoday")
    var stoptoday: String?,
    @SerializedName("terms")
    var terms: String?,
    @SerializedName("gbdelivery")
    var gbdelivery: String?,
    @SerializedName("gbtype")
    var gbtype : String?,
    @SerializedName("token")
    var token: String?,
    @SerializedName("dversion")
    var dversion: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("version")
    var version: String?,
    @SerializedName("deliverytime")
    var deliverytime: List<Int>?,
    @SerializedName("pickuptime")
    var pickuptime: List<Int>?,
    @SerializedName("address")
    var address: String?,
    @SerializedName("del_pckg")
    var del_pckg: String?,
    @SerializedName("pick_pckg")
    var pick_pckg: String?,
    @SerializedName("loginId")
    var loginId: String?
)