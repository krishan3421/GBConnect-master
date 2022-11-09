package com.gb.restaurant.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.order.CompOrderRequest
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.model.order.OrderRequest
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.ui.ConfirmTimeDialogActivity
import com.gb.restaurant.utils.Util
import javax.inject.Inject

class OrderViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var printLastOrder = MutableLiveData<Boolean>()

    var orderResponse = MutableLiveData<OrderResponse>()
    var orderStatusResponse = MutableLiveData<OrderStatusResponse>()
    fun getOrderResponse(orderRequest: OrderRequest, isPrintLastOrder: Boolean) {
        isLoading.value = true
        repository.getOrder(orderRequest,
            {
                orderResponse.value = it
                isLoading.value = false
                printLastOrder.value = isPrintLastOrder
            },

            {
                apiError.value = it
                isLoading.value = false
                printLastOrder.value = false

            })
    }

    fun getCompOrderResponse(compOrderRequest: CompOrderRequest) {
        isLoading.value = true
        repository.getCompOrder(compOrderRequest,
            {
                orderResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    /**
     * Adapter Callback
     */

    fun getOrderAt(position: Int): Data? {
        if (position < getOrderSize()) {
            return orderResponse.value?.data?.get(position)
        } else {
            return null
        }
    }

    fun getOrderSize(): Int {
        orderResponse.value?.data?.let {
            return it.size
        }
        return 0
    }


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