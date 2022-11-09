package com.gb.restaurant.model.register


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("email")
    val email: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("rest_name")
    val restName: String?,
    @SerializedName("rest_role")
    val restRole: String?,
    @SerializedName("rest_zip")
    val restZip: String?
)