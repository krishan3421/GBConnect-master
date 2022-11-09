package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.discount.adddiscount.AddDiscountRequest
import com.gb.restaurant.model.discount.adddiscount.AddDiscountResponse
import com.gb.restaurant.model.discount.editdiscount.EditDiscountRequest
import com.gb.restaurant.model.discount.editdiscount.EditDiscountResponse
import com.gb.restaurant.model.discount.getdiscount.Discount
import com.gb.restaurant.model.discount.getdiscount.DiscountRequest
import com.gb.restaurant.model.discount.getdiscount.GetDiscountResponse
import com.gb.restaurant.model.discount.rmdiscount.RmDiscountRequest
import com.gb.restaurant.model.discount.rmdiscount.RmDiscountResponse
import com.gb.restaurant.model.report.Data
import com.gb.restaurant.model.report.ReportRequest
import com.gb.restaurant.model.report.ReportResponse
import javax.inject.Inject

class CouponViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var getDiscountResponse = MutableLiveData<GetDiscountResponse>()
    var addDiscountResponse = MutableLiveData<AddDiscountResponse>()
    var rmDiscountResponse = MutableLiveData<RmDiscountResponse>()
    var editDiscountResponse = MutableLiveData<EditDiscountResponse>()

    fun editDiscount(editDiscountRequest: EditDiscountRequest) {
        isLoading.value = true
        repository.editDiscounts(editDiscountRequest,
            {
                editDiscountResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }
    fun rmDiscount(rmDiscountRequest: RmDiscountRequest) {
        isLoading.value = true
        repository.rmDiscounts(rmDiscountRequest,
            {
                rmDiscountResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    fun addDiscount(addDiscountRequest: AddDiscountRequest) {
        isLoading.value = true
        repository.addDiscounts(addDiscountRequest,
            {
                addDiscountResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }
    fun getDiscounts(discountRequest: DiscountRequest) {
        isLoading.value = true
        repository.getDiscounts(discountRequest,
            {
                getDiscountResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }

    fun getOfferAt(position: Int): Discount? {
        if (position < getOfferSize()) {
            return getDiscountResponse.value?.data?.get(position)
        } else {
            return null
        }
    }

    fun getOfferSize(): Int {
        getDiscountResponse.value?.data?.let {
            return it.size
        }
        return 0
    }
}