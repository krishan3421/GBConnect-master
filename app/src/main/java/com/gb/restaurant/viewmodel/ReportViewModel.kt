package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.report.Data
import com.gb.restaurant.model.report.ReportRequest
import com.gb.restaurant.model.report.ReportResponse
import javax.inject.Inject

class ReportViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var reportResponse = MutableLiveData<ReportResponse>()

    fun getReportResponse(reportRequest: ReportRequest) {
        isLoading.value = true
        repository.getReport(reportRequest,
            {
                reportResponse.value = it
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

    fun getReportAt(position: Int): Data? {
        if (position < getReportSize()) {
            return reportResponse.value?.data?.get(position)
        } else {
            return null
        }
    }

    fun getReportSize(): Int {
        reportResponse.value?.data?.let {
            return it.size
        }
        return 0
    }
}