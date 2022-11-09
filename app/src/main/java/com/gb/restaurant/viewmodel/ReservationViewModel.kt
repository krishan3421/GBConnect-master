package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.reservation.*
import com.gb.restaurant.model.status.EnquiryStatusRequest
import com.gb.restaurant.model.status.ReserStatusRequest
import com.gb.restaurant.model.status.StatusResponse
import javax.inject.Inject

class ReservationViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var response = MutableLiveData<ReservationResponse>()
    var resStatusResponse = MutableLiveData<StatusResponse>()
    var enquiryStatusResponse = MutableLiveData<StatusResponse>()
    var stopReserResponse = MutableLiveData<StopReservationResponse>()

    fun getReservationResponse(reservationRequest: ReservationRequest) {
        isLoading.value = true
        repository.reservationResponse(reservationRequest,
            {
                response.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    fun stopReservation(reservationStopRequest: ReservationStopRequest) {
        isLoading.value = true
        repository.stopReservation(reservationStopRequest,
            {
                stopReserResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }


    fun getReservationStatusRes(reserStatusRequest: ReserStatusRequest) {
        isLoading.value = true
        repository.reservationStatusResponse(reserStatusRequest,
            {
                resStatusResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    fun getEnquiryStatusRes(enquiryStatusRequest: EnquiryStatusRequest) {
        isLoading.value = true
        repository.enquiryStatusResponse(enquiryStatusRequest,
            {
                enquiryStatusResponse.value = it
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

    fun getReservationAt(position: Int): Data? {
        if (position < getReservationSize()) {
            return response.value?.data?.get(position)
        } else {
            return null
        }
    }

    fun getReservationSize(): Int {
        response.value?.data?.let {
            return it.size
        }
        return 0
    }
}