package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.bank.AddBankDetailRequest
import com.gb.restaurant.model.bank.AddBankDetailResponse
import com.gb.restaurant.model.bank.BankDetailRequest
import com.gb.restaurant.model.bank.GetBankDetailResponse
import com.gb.restaurant.model.stoporder.StopOrderRequest
import com.gb.restaurant.model.stoporder.StopOrderResponse
import com.gb.restaurant.model.updatesetting.UpdateSettingRequest
import com.gb.restaurant.model.updatesetting.UpdateSettingResponse
import javax.inject.Inject

class BankViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var getBankResponse = MutableLiveData<GetBankDetailResponse>()
    var addBankResponse = MutableLiveData<AddBankDetailResponse>()

    fun getBankDetail(bankDetailRequest: BankDetailRequest) {
        isLoading.value = true
        repository.getBankDetail(bankDetailRequest,
            {
                getBankResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    fun addBankDetail(addBankDetailRequest: AddBankDetailRequest) {
        isLoading.value = true
        repository.addBankDetail(addBankDetailRequest,
            {
                addBankResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

}