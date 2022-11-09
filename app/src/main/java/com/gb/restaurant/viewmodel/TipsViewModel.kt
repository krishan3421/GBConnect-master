package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.additem.AddOrderItemRequest
import com.gb.restaurant.model.additem.AddOrderItemResponse
import com.gb.restaurant.model.addordertips.OrderTipsRequest
import com.gb.restaurant.model.addordertips.OrderTipsResponse
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.order.OrderRequest
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.orderdetail.OrderDetailRequest
import com.gb.restaurant.model.orderdetail.OrderDetailResponse
import javax.inject.Inject

class TipsViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var addTipsResponse = MutableLiveData<OrderTipsResponse>()

    var addItemsOrderResponse = MutableLiveData<AddOrderItemResponse>()
    var orderResponse = MutableLiveData<OrderResponse>()
    var orderDetailResponse = MutableLiveData<OrderDetailResponse>()
    var orderStatusResponse = MutableLiveData<OrderStatusResponse>()
    fun addTips(orderTipsRequest: OrderTipsRequest) {
        isLoading.value = true
        repository.addorderTips(orderTipsRequest,
            {
                addTipsResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    fun addItemsOrder(addOrderItemRequest: AddOrderItemRequest) {
        isLoading.value = true
        repository.addItemsOrder(addOrderItemRequest,
            {
                addItemsOrderResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    fun getOrderResponse(orderRequest: OrderRequest) {
        isLoading.value = true
        repository.getOrder(orderRequest,
            {
                orderResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    fun getOrderDetailResponse(orderDetailRequest: OrderDetailRequest) {
        isLoading.value = true
        repository.getOrderDetail(orderDetailRequest,
            {
                orderDetailResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
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