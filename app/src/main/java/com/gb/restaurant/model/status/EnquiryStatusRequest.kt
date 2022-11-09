package com.gb.restaurant.model.status

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class EnquiryStatusRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY,
                                var restaurant_id:String = "", var service_type:String = Constant.SERVICE_TYPE.ENQUIRY_STATUS,
                                var enquiry_id:String = "", var reply:String = "", var status:String = Constant.ORDER_STATUS.CONFIRMED,
                                var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)