package com.gb.restaurant.model.register

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class RegisterRequest(var api_id:String =Constant.WEBSERVICE.API_ID, var api_key:String =Constant.WEBSERVICE.API_KEY, var name:String ="",
                           var email:String ="", var phone:String ="", var rest_name:String ="", var rest_zip:String ="", var rest_role:String ="",
                           var service_type:String =Constant.SERVICE_TYPE.REGISTER,
                           var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)