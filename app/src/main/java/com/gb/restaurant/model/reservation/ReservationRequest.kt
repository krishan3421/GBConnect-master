package com.gb.restaurant.model.reservation

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class ReservationRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY,
                              var restaurant_id:String = "", var service_type:String = Constant.SERVICE_TYPE.GET_RESERVATION,
                              var datefrom:String = "", var dateto:String = "",var search_type:String="Today",
                              var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)