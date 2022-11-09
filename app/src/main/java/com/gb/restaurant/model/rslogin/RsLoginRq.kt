package com.gb.restaurant.model.rslogin

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class RsLoginRq(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY, var login_id:String ="",
                     var login_pw:String ="", var service_type:String ="GetLogin", var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "",
                     var devicetype:String = Constant.DEVICE.DEVICE_TYPE)