package com.gb.restaurant.model.reservation


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("bookingtime")
    val bookingtime: String?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("date2")
    val date2: String?,
    @SerializedName("details")
    val details: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("location")
    val location: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("peoples")
    val peoples: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("reply")
    var reply: String?="",
    @SerializedName("status")
    val status: String?,
    @SerializedName("today")
    val today: String?
)