package com.gb.restaurant.model.additem

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.model.Item

data class AddOrderItemRequest(val api_id:String = Constant.WEBSERVICE.API_ID,
                               val api_key:String = Constant.WEBSERVICE.API_KEY,
                               var restaurant_id:String = "",
                               var order_id:String = "",
                               var card:String = "",
                               var cvv:String ="",
                               var expiry:String ="",
                               var billingzip:String ="",
                               var cardholder:String ="",
                               var service_type:String =Constant.SERVICE_TYPE.ADD_ORDER_ITEMS,
                               var itemslist:MutableList<Item> = mutableListOf(),var newcard:String = "No",
                               var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)