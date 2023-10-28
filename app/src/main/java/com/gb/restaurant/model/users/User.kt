package com.gb.restaurant.model.users

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    @SerializedName("date")
    val date: String?,
    @SerializedName("gb_id")
    val gbId: String?,
    @SerializedName("gb_pass")
    val gbPass: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("rid")
    val rid: String?,
    @SerializedName("status")
    val status: String?
): Parcelable