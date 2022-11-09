package com.gb.restaurant.model.additem


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("id")
    val id: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("payment")
    val payment : String?,
    @SerializedName("result")
    val result : String?
)