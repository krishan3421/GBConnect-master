package com.gb.restaurant.di.module

import com.gb.restaurant.Constant
import okhttp3.HttpUrl
import retrofit2.Retrofit
import java.lang.reflect.Field

object RetrofitHolder {
     var retrofit :Retrofit?=null

     fun changeBaseUrl(type:String, retro :Retrofit?){
          if(type == "GB") {
               println("type>>>>>>>>>>, $type")
               val field: Field = Retrofit::class.java.getDeclaredField("baseUrl")
               field.isAccessible = true
               val newHttpUrl = HttpUrl.parse(Constant.GD_URL)
               field.set(retro, newHttpUrl)
          }
     }
}