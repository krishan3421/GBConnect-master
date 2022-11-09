package com.gb.restaurant.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.model.adduser.AddUserRequest
import com.gb.restaurant.model.adduser.AddUserResponse
import com.gb.restaurant.model.forgot.ForgotPassRequest
import com.gb.restaurant.model.forgot.ForgotPassResponse
import com.gb.restaurant.model.logout.LogoutRequest
import com.gb.restaurant.model.logout.LogoutResponse
import com.gb.restaurant.model.register.RegisterRequest
import com.gb.restaurant.model.register.RegisterResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.rslogin.RsLoginRq
import com.gb.restaurant.model.users.User
import com.gb.restaurant.model.users.UserListReponse
import com.gb.restaurant.model.users.UsersRequest
import com.gb.restaurant.model.users.edituser.EditUserRequest
import com.gb.restaurant.model.users.edituser.EditUserResponse
import com.gb.restaurant.model.users.rmuser.RmUserRequest
import com.gb.restaurant.model.users.rmuser.RmUserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class RsLoginViewModel :ViewModel(){

    @Inject
    lateinit var repository: GBRepository

    var isLoading = MutableLiveData<Boolean>()

    var apiError = MutableLiveData<String>()

    var loginResponse = MutableLiveData<RsLoginResponse>()
    var registerResponse = MutableLiveData<RegisterResponse>()
    var addUserResponse = MutableLiveData<AddUserResponse>()
    var getGbUserResponse = MutableLiveData<UserListReponse>()
    var rmGbUserResponse = MutableLiveData<RmUserResponse>()
    var editGbUserResponse = MutableLiveData<EditUserResponse>()
    var logoutResponse = MutableLiveData<LogoutResponse>()
    var forgotResponse = MutableLiveData<ForgotPassResponse>()
    fun forgotPass(forgotPassRequest: ForgotPassRequest) {
        isLoading.value = true
        repository.forgotPass(forgotPassRequest,
            {
                forgotResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }
    fun logout(logoutRequest: LogoutRequest) {
        isLoading.value = true
        repository.logout(logoutRequest,
            {
                logoutResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }
    fun editGbUser(editUserRequest: EditUserRequest) {
        isLoading.value = true
        repository.editGbUser(editUserRequest,
            {
                editGbUserResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }
    fun rmGbUsers(rmUserRequest: RmUserRequest) {
        isLoading.value = true
        repository.rmGbUser(rmUserRequest,
            {
                rmGbUserResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }
    fun getGbUsers(usersRequest: UsersRequest) {
        isLoading.value = true
        repository.getGbUser(usersRequest,
            {
                getGbUserResponse.value = it
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

    fun getUserAt(position: Int): User? {
        if (position < getUsersSize()) {
            return getGbUserResponse.value?.data?.get(position)
        } else {
            return null
        }
    }

     fun getUsersSize(): Int {
        getGbUserResponse.value?.data?.let {
            return it.size
        }
        return 0
    }
    fun addGbUser(addUserRequest: AddUserRequest) {
        isLoading.value = true
        repository.addGbUser(addUserRequest,
            {
                addUserResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }
    fun getLoginResponse(rsLoginRq: RsLoginRq) {
        isLoading.value = true
        viewModelScope.launch{
                var response=   loginService(rsLoginRq)
                if(response.isSuccessful){
                    loginResponse.value=response.body()
                    isLoading.value=false
                }else{
                    apiError.value= response.message()
                    isLoading.value=false
                }
           println("inside>>>>>>>>viewModelScope")
        }
        println("outside>>>>>>>>viewModelScope")
    }

    private suspend fun loginService(rsLoginRq: RsLoginRq):Response<RsLoginResponse>{
       return withContext(Dispatchers.IO){
           repository.reLogin(rsLoginRq)
        }
    }

    fun registerUser(registerRequest: RegisterRequest) {
        isLoading.value = true
        repository.registerUser(registerRequest,
            {
                registerResponse.value = it
                isLoading.value = false
            },

            {
                apiError.value = it
                isLoading.value = false
            })
    }
}