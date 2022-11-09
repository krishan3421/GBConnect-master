package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.resetpass.ResetPassRequest
import com.gb.restaurant.model.resetpass.ResetPassResponse
import javax.inject.Inject

class ResetPassViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var resetPassResponse = MutableLiveData<ResetPassResponse>()

    fun resetPass(resetPassRequest: ResetPassRequest) {
        isLoading.value = true
        repository.resetPassword(resetPassRequest,
            {
                resetPassResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }


}