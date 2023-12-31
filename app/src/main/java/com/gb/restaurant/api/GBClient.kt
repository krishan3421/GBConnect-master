package com.gb.restaurant.api

import com.gb.restaurant.Constant
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
import com.gb.restaurant.model.orderstatus.ResturantStatusResponse
import com.gb.restaurant.model.orderstatus.StatusRequest
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
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GBClient {

    @POST(Constant.SERVICE_API.RESTAURANT_LOGIN)
   suspend fun restaurantLogin(@Body rsLoginRq: RsLoginRq): Response<RsLoginResponse>

    @POST(Constant.SERVICE_API.GET_ALL_ORDER)
    fun getOrder(@Body orderRequest: OrderRequest): Call<OrderResponse>

    @POST(Constant.SERVICE_API.GET_ALL_ORDER)
    fun getCompOrder(@Body compOrderRequest: CompOrderRequest): Call<OrderResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getOrderDetail(@Body orderDetailRequest: OrderDetailRequest): Call<OrderDetailResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getReport(@Body reportRequest: ReportRequest): Call<ReportResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getActiveOrderList(@Body activeOrderRequest: ActiveOrderRequest): Call<ActiveOrderResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getOrderSearch(@Body orderSearchRequest: OrderSearchRequest): Call<SearchOrderResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getCallBack(@Body supportRequest: SupportRequest): Call<SupportResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun stopOrderToday(@Body stopOrderRequest: StopOrderRequest): Call<StopOrderResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun updateSetting(@Body updateSettingRequest: UpdateSettingRequest): Call<UpdateSettingResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun orderStatus(@Body orderStatusRequest: OrderStatusRequest): Call<OrderStatusResponse>

    @POST(Constant.SERVICE_API.ADD_ORDER_TIPS)
    fun addOrderTips(@Body orderTipsRequest: OrderTipsRequest): Call<OrderTipsResponse>

    @POST(Constant.SERVICE_API.ADD_ORDER_ITEMS)
    fun addItemsOrder(@Body addOrderItemRequest: AddOrderItemRequest): Call<AddOrderItemResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun resetPassword(@Body resetPassRequest: ResetPassRequest): Call<ResetPassResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun reservationResponse(@Body reservationRequest: ReservationRequest): Call<ReservationResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun reservationStatus(@Body reserStatusRequest: ReserStatusRequest): Call<StatusResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun enquiryStatus(@Body enquiryStatusRequest: EnquiryStatusRequest): Call<StatusResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun registerUser(@Body registerRequest: RegisterRequest): Call<RegisterResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun stopReservation(@Body reservationStopRequest: ReservationStopRequest): Call<StopReservationResponse>
    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getDailySummery(@Body dailySumRequest: DailySumRequest): Call<DailySummResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun addGBUser(@Body addUserRequest: AddUserRequest): Call<AddUserResponse>
    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getGBUser(@Body usersRequest: UsersRequest): Call<UserListReponse>
    @POST(Constant.SERVICE_API.COMMON_URL)
    fun rmGBUser(@Body rmUserRequest: RmUserRequest): Call<RmUserResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun editGBUser(@Body editUserRequest: EditUserRequest): Call<EditUserResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getDiscounts(@Body discountRequest: DiscountRequest): Call<GetDiscountResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun addDiscounts(@Body addDiscountRequest: AddDiscountRequest): Call<AddDiscountResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun rmDiscounts(@Body rmDiscountRequest: RmDiscountRequest): Call<RmDiscountResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun editDiscount(@Body editDiscountRequest: EditDiscountRequest): Call<EditDiscountResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun logout(@Body logoutRequest: LogoutRequest): Call<LogoutResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun forgotPass(@Body forgotPassRequest: ForgotPassRequest): Call<ForgotPassResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getBankDetail(@Body bankDetailRequest: BankDetailRequest): Call<GetBankDetailResponse>

    @POST(Constant.SERVICE_API.COMMON_URL)
    fun addBankDetail(@Body addBankDetailRequest: AddBankDetailRequest): Call<AddBankDetailResponse>
    @POST(Constant.SERVICE_API.COMMON_URL)
    fun getRestaurantStatus(@Body statusRequest: StatusRequest): Call<ResturantStatusResponse>
}