package com.gb.restaurant.model.report

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.utils.Util

data class ReportRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY, var restaurant_id:String = "",
                         var service_type:String = Constant.SERVICE_TYPE.GET_REOPRT, var year:String = Util.getCurrentYear()!!, var month:String = Util.getCurrentMonth()!!,
                         var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)