package com.gb.restaurant.model.users.edituser

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class EditUserRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY, var restaurant_id:String = "",
                           var service_type:String = Constant.SERVICE_TYPE.EDIT_GB_USER, var userid:String = "",var name:String="",var userpass:String="",
                           var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)