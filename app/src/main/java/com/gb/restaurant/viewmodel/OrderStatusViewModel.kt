package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import javax.inject.Inject

class OrderStatusViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var orderStatusResponse = MutableLiveData<OrderStatusResponse>()

    fun orderStatus(orderStatusRequest: OrderStatusRequest) {
        isLoading.value = true
        repository.orderStatus(orderStatusRequest,
            {
                orderStatusResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }


}