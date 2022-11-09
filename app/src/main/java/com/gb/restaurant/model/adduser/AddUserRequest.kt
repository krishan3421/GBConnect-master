package com.gb.restaurant.model.adduser

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class AddUserRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY, var restaurant_id:String = "",
                          var service_type:String = Constant.SERVICE_TYPE.ADD_GB_USER,var name:String="",var userid:String="",var userpass:String="",
                          var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)