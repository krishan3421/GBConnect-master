package com.gb.restaurant.model.bank


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("message")
    val message: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("bank")
    val bank: String?,
    @SerializedName("routing")
    val routing: String?,
    @SerializedName("account")
    val account: String?,
    @SerializedName("cheq_img")
    val cheq_img: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("comment")
    val comment: String?
)