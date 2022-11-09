package com.gb.restaurant.model.order

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class OrderRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY,
                        var restaurant_id:String = "", var service_type:String = Constant.SERVICE_TYPE.GET_ACTIVE_ORDER,
                        var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)