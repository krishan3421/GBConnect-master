package com.gb.restaurant.push

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PushMessage(var title:String="", var body:String="", var type:String= TYPE.OrderNew.toString(), var subtype:String=""):
    Parcelable