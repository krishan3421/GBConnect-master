package com.gb.restaurant.model.reservation

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class ReservationStopRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY,
                                  var restaurant_id:String = "", var service_type:String = Constant.SERVICE_TYPE.GET_RESERVATION_STOP,
                                  var datefrom:String = "", var dateto:String = "", var reservation_id:String = "", var stop:String = Constant.RESERVATION_STOP.TODAY,
                                  var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)
//stop : Today / Day / Between