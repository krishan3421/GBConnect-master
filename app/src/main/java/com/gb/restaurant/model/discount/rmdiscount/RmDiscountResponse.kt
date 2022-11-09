package com.gb.restaurant.model.discount.rmdiscount


import com.google.gson.annotations.SerializedName

data class RmDiscountResponse(
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)