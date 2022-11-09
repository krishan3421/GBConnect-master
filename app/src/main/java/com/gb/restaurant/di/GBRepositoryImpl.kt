package com.gb.restaurant.di

import com.gb.restaurant.api.GBClient
import com.gb.restaurant.model.additem.AddOrderItemRequest
import com.gb.restaurant.model.additem.AddOrderItemResponse
import com.gb.restaurant.model.addordertips.OrderTipsRequest
import com.gb.restaurant.model.addordertips.OrderTipsResponse
import com.gb.restaurant.model.addtips.ActiveOrderRequest
import com.gb.restaurant.model.addtips.ActiveOrderResponse
import com.gb.restaurant.model.addtips.OrderSearchRequest
import com.gb.restaurant.model.addtips.SearchOrderResponse
import com.gb.restaurant.model.adduser.AddUserRequest
import com.gb.restaurant.model.adduser.AddUserResponse
import com.gb.restaurant.model.bank.AddBankDetailRequest
import com.gb.restaurant.model.bank.AddBankDetailResponse
import com.gb.restaurant.model.bank.BankDetailRequest
import com.gb.restaurant.model.bank.GetBankDetailResponse
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.daily.DailySumRequest
import com.gb.restaurant.model.daily.DailySummResponse
import com.gb.restaurant.model.discount.adddiscount.AddDiscountRequest
import com.gb.restaurant.model.discount.adddiscount.AddDiscountResponse
import com.gb.restaurant.model.discount.editdiscount.EditDiscountRequest
import com.gb.restaurant.model.discount.editdiscount.EditDiscountResponse
import com.gb.restaurant.model.discount.getdiscount.DiscountRequest
import com.gb.restaurant.model.discount.getdiscount.GetDiscountResponse
import com.gb.restaurant.model.discount.rmdiscount.RmDiscountRequest
import com.gb.restaurant.model.discount.rmdiscount.RmDiscountResponse
import com.gb.restaurant.model.forgot.ForgotPassRequest
import com.gb.restaurant.model.forgot.ForgotPassResponse
import com.gb.restaurant.model.logout.LogoutRequest
import com.gb.restaurant.model.logout.LogoutResponse
import com.gb.restaurant.model.order.CompOrderRequest
import com.gb.restaurant.model.order.OrderRequest
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.orderdetail.OrderDetailRequest
import com.gb.restaurant.model.orderdetail.OrderDetailResponse
import com.gb.restaurant.model.register.RegisterRequest
import com.gb.restaurant.model.register.RegisterResponse
import com.gb.restaurant.model.report.ReportRequest
import com.gb.restaurant.model.report.ReportResponse
import com.gb.restaurant.model.reservation.ReservationRequest
import com.gb.restaurant.model.reservation.ReservationResponse
import com.gb.restaurant.model.reservation.ReservationStopRequest
import com.gb.restaurant.model.reservation.StopReservationResponse
import com.gb.restaurant.model.resetpass.ResetPassRequest
import com.gb.restaurant.model.resetpass.ResetPassResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.rslogin.RsLoginRq
import com.gb.restaurant.model.status.EnquiryStatusRequest
import com.gb.restaurant.model.status.ReserStatusRequest
import com.gb.restaurant.model.status.StatusResponse
import com.gb.restaurant.model.stoporder.StopOrderRequest
import com.gb.restaurant.model.stoporder.StopOrderResponse
import com.gb.restaurant.model.support.SupportRequest
import com.gb.restaurant.model.support.SupportResponse
import com.gb.restaurant.model.updatesetting.UpdateSettingRequest
import com.gb.restaurant.model.updatesetting.UpdateSettingResponse
import com.gb.restaurant.model.users.UserListReponse
import com.gb.restaurant.model.users.UsersRequest
import com.gb.restaurant.model.users.edituser.EditUserRequest
import com.gb.restaurant.model.users.edituser.EditUserResponse
import com.gb.restaurant.model.users.rmuser.RmUserRequest
import com.gb.restaurant.model.users.rmuser.RmUserResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Response



/**
 * Created by Krishan on 08/20/2019
 */

class GBRepositoryImpl(private val apiService: GBClient) : GBRepository {

    companion object{
        private const val ERROR_500 :String="Unable to connect to server. Please try again after sometime."
    }
   override suspend fun reLogin(rsLoginRq: RsLoginRq): Response<RsLoginResponse> {
      return  apiService.restaurantLogin(rsLoginRq)

    }


    override fun getOrder(orderRequest: OrderRequest, successHandler: (OrderResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getOrder(orderRequest).enqueue(object:retrofit2.Callback<OrderResponse>{

            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun getCompOrder(compOrderRequest: CompOrderRequest, successHandler: (OrderResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getCompOrder(compOrderRequest).enqueue(object:retrofit2.Callback<OrderResponse>{

            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun getOrderDetail(orderDetailRequest: OrderDetailRequest, successHandler: (OrderDetailResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getOrderDetail(orderDetailRequest).enqueue(object:retrofit2.Callback<OrderDetailResponse>{

            override fun onResponse(call: Call<OrderDetailResponse>, response: Response<OrderDetailResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<OrderDetailResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun getReport(reportRequest: ReportRequest, successHandler: (ReportResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getReport(reportRequest).enqueue(object:retrofit2.Callback<ReportResponse>{

            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun getCallBack(supportRequest: SupportRequest, successHandler: (SupportResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getCallBack(supportRequest).enqueue(object:retrofit2.Callback<SupportResponse>{

            override fun onResponse(call: Call<SupportResponse>, response: Response<SupportResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<SupportResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun getStopOrder(stopOrderRequest: StopOrderRequest, successHandler: (StopOrderResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.stopOrderToday(stopOrderRequest).enqueue(object:retrofit2.Callback<StopOrderResponse>{
            override fun onResponse(call: Call<StopOrderResponse>, response: Response<StopOrderResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<StopOrderResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun updateSetting(updateSettingRequest: UpdateSettingRequest, successHandler: (UpdateSettingResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.updateSetting(updateSettingRequest).enqueue(object:retrofit2.Callback<UpdateSettingResponse>{

            override fun onResponse(call: Call<UpdateSettingResponse>, response: Response<UpdateSettingResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<UpdateSettingResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }


    override fun orderStatus(orderStatusRequest: OrderStatusRequest, successHandler: (OrderStatusResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.orderStatus(orderStatusRequest).enqueue(object:retrofit2.Callback<OrderStatusResponse>{

            override fun onResponse(call: Call<OrderStatusResponse>, response: Response<OrderStatusResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<OrderStatusResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun addorderTips(orderTipsRequest: OrderTipsRequest, successHandler: (OrderTipsResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.addOrderTips(orderTipsRequest).enqueue(object:retrofit2.Callback<OrderTipsResponse>{

            override fun onResponse(call: Call<OrderTipsResponse>, response: Response<OrderTipsResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<OrderTipsResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }


    override fun addItemsOrder(addOrderItemRequest: AddOrderItemRequest, successHandler: (AddOrderItemResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.addItemsOrder(addOrderItemRequest).enqueue(object:retrofit2.Callback<AddOrderItemResponse>{

            override fun onResponse(call: Call<AddOrderItemResponse>, response: Response<AddOrderItemResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<AddOrderItemResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun resetPassword(resetPassRequest: ResetPassRequest, successHandler: (ResetPassResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.resetPassword(resetPassRequest).enqueue(object:retrofit2.Callback<ResetPassResponse>{

            override fun onResponse(call: Call<ResetPassResponse>, response: Response<ResetPassResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<ResetPassResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun reservationResponse(reservationRequest: ReservationRequest, successHandler: (ReservationResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.reservationResponse(reservationRequest).enqueue(object:retrofit2.Callback<ReservationResponse>{

            override fun onResponse(call: Call<ReservationResponse>, response: Response<ReservationResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<ReservationResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun reservationStatusResponse(reserStatusRequest: ReserStatusRequest, successHandler: (StatusResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.reservationStatus(reserStatusRequest).enqueue(object:retrofit2.Callback<StatusResponse>{

            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun enquiryStatusResponse(enquiryStatusRequest: EnquiryStatusRequest, successHandler: (StatusResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.enquiryStatus(enquiryStatusRequest).enqueue(object:retrofit2.Callback<StatusResponse>{

            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                println("error>>>> ${Gson().toJson(t)}")
                failureHandler(t.message)
            }

        })
    }

    override fun registerUser(registerRequest: RegisterRequest, successHandler: (RegisterResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.registerUser(registerRequest).enqueue(object:retrofit2.Callback<RegisterResponse>{

            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun getActiveOrderList(activeOrderRequest: ActiveOrderRequest, successHandler: (ActiveOrderResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getActiveOrderList(activeOrderRequest).enqueue(object:retrofit2.Callback<ActiveOrderResponse>{

            override fun onResponse(call: Call<ActiveOrderResponse>, response: Response<ActiveOrderResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<ActiveOrderResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun getOrderSearch(orderSearchRequest: OrderSearchRequest, successHandler: (SearchOrderResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getOrderSearch(orderSearchRequest).enqueue(object:retrofit2.Callback<SearchOrderResponse>{

            override fun onResponse(call: Call<SearchOrderResponse>, response: Response<SearchOrderResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<SearchOrderResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun stopReservation(reservationStopRequest: ReservationStopRequest, successHandler: (StopReservationResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.stopReservation(reservationStopRequest).enqueue(object:retrofit2.Callback<StopReservationResponse>{

            override fun onResponse(call: Call<StopReservationResponse>, response: Response<StopReservationResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<StopReservationResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun getDailySummery(dailySumRequest: DailySumRequest, successHandler: (DailySummResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getDailySummery(dailySumRequest).enqueue(object:retrofit2.Callback<DailySummResponse>{

            override fun onResponse(call: Call<DailySummResponse>, response: Response<DailySummResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<DailySummResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun addGbUser(addUserRequest: AddUserRequest, successHandler: (AddUserResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.addGBUser(addUserRequest).enqueue(object:retrofit2.Callback<AddUserResponse>{

            override fun onResponse(call: Call<AddUserResponse>, response: Response<AddUserResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<AddUserResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun getGbUser(usersRequest: UsersRequest, successHandler: (UserListReponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getGBUser(usersRequest).enqueue(object:retrofit2.Callback<UserListReponse>{

            override fun onResponse(call: Call<UserListReponse>, response: Response<UserListReponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<UserListReponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun rmGbUser(rmUserRequest: RmUserRequest, successHandler: (RmUserResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.rmGBUser(rmUserRequest).enqueue(object:retrofit2.Callback<RmUserResponse>{

            override fun onResponse(call: Call<RmUserResponse>, response: Response<RmUserResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<RmUserResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun editGbUser(editUserRequest: EditUserRequest, successHandler: (EditUserResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.editGBUser(editUserRequest).enqueue(object:retrofit2.Callback<EditUserResponse>{

            override fun onResponse(call: Call<EditUserResponse>, response: Response<EditUserResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<EditUserResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun getDiscounts(discountRequest: DiscountRequest, successHandler: (GetDiscountResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getDiscounts(discountRequest).enqueue(object:retrofit2.Callback<GetDiscountResponse>{

            override fun onResponse(call: Call<GetDiscountResponse>, response: Response<GetDiscountResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<GetDiscountResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun addDiscounts(addDiscountRequest: AddDiscountRequest, successHandler: (AddDiscountResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.addDiscounts(addDiscountRequest).enqueue(object:retrofit2.Callback<AddDiscountResponse>{

            override fun onResponse(call: Call<AddDiscountResponse>, response: Response<AddDiscountResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<AddDiscountResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun rmDiscounts(rmDiscountRequest: RmDiscountRequest, successHandler: (RmDiscountResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.rmDiscounts(rmDiscountRequest).enqueue(object:retrofit2.Callback<RmDiscountResponse>{

            override fun onResponse(call: Call<RmDiscountResponse>, response: Response<RmDiscountResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<RmDiscountResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun editDiscounts(editDiscountRequest: EditDiscountRequest, successHandler: (EditDiscountResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.editDiscount(editDiscountRequest).enqueue(object:retrofit2.Callback<EditDiscountResponse>{

            override fun onResponse(call: Call<EditDiscountResponse>, response: Response<EditDiscountResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<EditDiscountResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun logout(logoutRequest: LogoutRequest, successHandler: (LogoutResponse) -> Unit,failureHandler: (String?) -> Unit) {
        apiService.logout(logoutRequest).enqueue(object:retrofit2.Callback<LogoutResponse>{

            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun forgotPass(forgotPassRequest: ForgotPassRequest, successHandler: (ForgotPassResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.forgotPass(forgotPassRequest).enqueue(object:retrofit2.Callback<ForgotPassResponse>{

            override fun onResponse(call: Call<ForgotPassResponse>, response: Response<ForgotPassResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<ForgotPassResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun getBankDetail(bankDetailRequest: BankDetailRequest, successHandler: (GetBankDetailResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.getBankDetail(bankDetailRequest).enqueue(object:retrofit2.Callback<GetBankDetailResponse>{

            override fun onResponse(call: Call<GetBankDetailResponse>, response: Response<GetBankDetailResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<GetBankDetailResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }

    override fun addBankDetail(addBankDetailRequest: AddBankDetailRequest, successHandler: (AddBankDetailResponse) -> Unit, failureHandler: (String?) -> Unit) {
        apiService.addBankDetail(addBankDetailRequest).enqueue(object:retrofit2.Callback<AddBankDetailResponse>{

            override fun onResponse(call: Call<AddBankDetailResponse>, response: Response<AddBankDetailResponse>) {
                if(response?.body()!=null){
                    response?.body()?.let {
                        successHandler(it)
                    }
                }else{
                    if(response.code() ==500){
                        failureHandler(ERROR_500)
                    }else{
                        failureHandler("Error Code ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<AddBankDetailResponse>, t: Throwable) {
                failureHandler(t.message)
            }

        })
    }


}
