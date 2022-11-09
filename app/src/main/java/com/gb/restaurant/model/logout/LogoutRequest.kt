package com.gb.restaurant.model.logout

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class LogoutRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY,
                         var login_id:String = "", var service_type:String = Constant.SERVICE_TYPE.LOGOUT,var login_key:String="",
                         var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)