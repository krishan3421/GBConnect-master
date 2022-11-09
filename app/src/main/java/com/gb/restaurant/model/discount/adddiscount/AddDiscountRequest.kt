package com.gb.restaurant.model.discount.adddiscount

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class AddDiscountRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY,
                              var restaurant_id:String = "", var service_type:String = Constant.SERVICE_TYPE.ADD_DISCOUNTS,
                              var minamount:String="",var discounttype:String="",var discounts:String="",
                              var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)