package com.gb.restaurant.model.updatesetting

import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

data class UpdateSettingRequest(val api_id:String = Constant.WEBSERVICE.API_ID, val api_key:String = Constant.WEBSERVICE.API_KEY,
                                var service_type:String = Constant.SERVICE_TYPE.GET_UPDATE_SETTING, var restaurant_id:String = "",
                                var pickup:String = "", var delivery:String = "", var miles:String = "", var mindelivery:String = "",
                                var deliverycharge:String = "", var deliverychargetype:String = "", var gbdelivery:String = Constant.GB_DELIVERY.SELF,
                                var DeviceToken:String = MyApp.instance.deviceToken, var deviceversion:String = "", var devicetype:String = Constant.DEVICE.DEVICE_TYPE)