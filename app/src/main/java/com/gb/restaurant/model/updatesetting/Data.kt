package com.gb.restaurant.model.updatesetting


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("setting")
    val setting: Setting?
)