package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.stoporder.StopOrderRequest
import com.gb.restaurant.model.stoporder.StopOrderResponse
import com.gb.restaurant.model.updatesetting.UpdateSettingRequest
import com.gb.restaurant.model.updatesetting.UpdateSettingResponse
import javax.inject.Inject

class DestinViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var stopOrderResponse = MutableLiveData<StopOrderResponse>()
    var updateSettingResponse = MutableLiveData<UpdateSettingResponse>()

    fun stopOrderToday(stopOrderRequest: StopOrderRequest) {
        isLoading.value = true
        repository.getStopOrder(stopOrderRequest,
            {
                stopOrderResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    fun updateSetting(updateSettingRequest: UpdateSettingRequest) {
        isLoading.value = true
        repository.updateSetting(updateSettingRequest,
            {
                updateSettingResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

}