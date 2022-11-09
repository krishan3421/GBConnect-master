package com.gb.restaurant.model.bank

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class AddBankDetailRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY
                                , var restaurant_id:String = "", var service_type:String =Constant.SERVICE_TYPE.ADD_BANK_DETAIL,
                                var name:String="", var bank:String="",var routing:String="",var account:String="",var cheque:String="",
                                var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)