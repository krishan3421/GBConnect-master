package com.gb.restaurant.model.users


import com.google.gson.annotations.SerializedName

data class UserListReponse(
    @SerializedName("data")
    val data: List<User>?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("status")
    val status: String?
)