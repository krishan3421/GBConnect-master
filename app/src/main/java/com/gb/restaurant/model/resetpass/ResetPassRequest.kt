package com.gb.restaurant.model.resetpass

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class ResetPassRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY, var restaurant_id:String = "",
                            var service_type:String = Constant.SERVICE_TYPE.RESETPASSWORD, var user_id:String = "", var password:String = "",
                            var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)