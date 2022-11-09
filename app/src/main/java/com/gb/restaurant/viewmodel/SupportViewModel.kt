package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.support.SupportRequest
import com.gb.restaurant.model.support.SupportResponse
import javax.inject.Inject

class SupportViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var supportResponse = MutableLiveData<SupportResponse>()

    fun getCallBack(supportRequest: SupportRequest) {
        isLoading.value = true
        repository.getCallBack(supportRequest,
            {
                supportResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

}