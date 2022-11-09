package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.additem.AddOrderItemRequest
import com.gb.restaurant.model.additem.AddOrderItemResponse
import com.gb.restaurant.model.addordertips.OrderTipsRequest
import com.gb.restaurant.model.addordertips.OrderTipsResponse
import com.gb.restaurant.model.addtips.*
import com.gb.restaurant.model.daily.DailySumRequest
import com.gb.restaurant.model.daily.DailySummResponse
import com.gb.restaurant.model.daily.Summary
import javax.inject.Inject

class AddTipsViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var response = MutableLiveData<ActiveOrderResponse>()
    var orderSearchResp = MutableLiveData<SearchOrderResponse>()
    var addTipsResponse = MutableLiveData<OrderTipsResponse>()
    var addItemsOrderResponse = MutableLiveData<AddOrderItemResponse>()
    var dailySummResponse = MutableLiveData<DailySummResponse>()

    fun getDailySumm(dailySumRequest: DailySumRequest) {
        isLoading.value = true
        repository.getDailySummery(dailySumRequest,
            {
                dailySummResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }
    /**
     *Summery Adapter Callback
     */

    fun getDailySummAt(position: Int): Summary? {
        return if (position < getDailySummSize()) {
            dailySummResponse.value?.data?.summaryList?.get(position)
        } else {
            null
        }
    }

    fun getDailySummSize(): Int {
        dailySummResponse.value?.data?.summaryList?.let {
            return it?.size!!
        }
        return 0
    }

    fun getGBDailySummAt(position: Int): Summary? {
        return if (position < getDailySummSize()) {
            dailySummResponse.value?.data?.gbDelivery?.summary?.get(position)
        } else {
            null
        }
    }

    fun getGBDailySummSize(): Int {
        dailySummResponse.value?.data?.gbDelivery?.summary?.let {
            return it?.size!!
        }
        return 0
    }
    fun getActiveOrderList(activeOrderRequest: ActiveOrderRequest) {
        isLoading.value = true
        repository.getActiveOrderList(activeOrderRequest,
            {
                response.value = it
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

    fun getOrderSearch(orderSearchRequest: OrderSearchRequest) {
        isLoading.value = true
        repository.getOrderSearch(orderSearchRequest,
            {
                if(it?.data!=null){
                    var dataArray= mutableListOf<Data>(it?.data)
                    var activeOrderResponse =ActiveOrderResponse(dataArray,it.result,it.status)
                   response.value=activeOrderResponse
                }else{
                    var dataArray= mutableListOf<Data>()
                    var activeOrderResponse =ActiveOrderResponse(dataArray,"","")
                    response.value=activeOrderResponse
                }
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

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

    /**
     * Adapter Callback
     */

    fun getActiveOrderAt(position: Int): Data? {
        return if (position < getActiveOrderSize()) {
            response.value?.data?.get(position)
        } else {
            null
        }
    }

    fun getActiveOrderSize(): Int {
        response.value?.data?.let {
            return it.size
        }
        return 0
    }
}