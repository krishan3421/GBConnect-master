package com.gb.restaurant.model.confirmorder

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class OrderStatusRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY
                              , var restaurant_id:String = "", var order_id:String= "", var service_type:String =Constant.SERVICE_TYPE.CONFIRM_ORDER,
                              var status:String =Constant.ORDER_STATUS.CONFIRMED, var readytime:String="", var details:String="",var refund:String="",
                              var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)