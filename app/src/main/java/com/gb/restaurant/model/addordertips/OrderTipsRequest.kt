package com.gb.restaurant.model.addordertips

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class OrderTipsRequest(val api_id:String = Constant.WEBSERVICE.API_ID
                            , val api_key:String = Constant.WEBSERVICE.API_KEY,
                            var restaurant_id:String = "",
                            var order_id:String = "", var order_tips:String ="",
                            var card:String = "", var cvv:String ="", var service_type:String =Constant.SERVICE_TYPE.ADD_ORDER_TIPS,
                            var expiry:String ="", var billingzip:String ="",var cardholder:String ="",var newcard:String = "",
                            var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)