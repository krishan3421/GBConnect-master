package com.gb.restaurant.di

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
import retrofit2.Response

/**
 * Created by Krishan on 08/20/2019.
 */
interface GBRepository {

   suspend fun reLogin(rsLoginRq: RsLoginRq):Response<RsLoginResponse>
    fun getOrder(orderRequest: OrderRequest, successHandler: (OrderResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getCompOrder(compOrderRequest: CompOrderRequest, successHandler: (OrderResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getOrderDetail(orderDetailRequest: OrderDetailRequest, successHandler: (OrderDetailResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getReport(reportRequest: ReportRequest, successHandler: (ReportResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getCallBack(supportRequest: SupportRequest, successHandler: (SupportResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getStopOrder(stopOrderRequest: StopOrderRequest, successHandler: (StopOrderResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun updateSetting(updateSettingRequest: UpdateSettingRequest, successHandler: (UpdateSettingResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun orderStatus(orderStatusRequest: OrderStatusRequest, successHandler: (OrderStatusResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun addorderTips(orderTipsRequest: OrderTipsRequest, successHandler: (OrderTipsResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun addItemsOrder(addOrderItemRequest: AddOrderItemRequest, successHandler: (AddOrderItemResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun resetPassword(resetPassRequest: ResetPassRequest, successHandler: (ResetPassResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun reservationResponse(reservationRequest: ReservationRequest, successHandler: (ReservationResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun reservationStatusResponse(reserStatusRequest: ReserStatusRequest, successHandler: (StatusResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun enquiryStatusResponse(enquiryStatusRequest: EnquiryStatusRequest, successHandler: (StatusResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun registerUser(registerRequest: RegisterRequest, successHandler: (RegisterResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getActiveOrderList(activeOrderRequest: ActiveOrderRequest, successHandler: (ActiveOrderResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getOrderSearch(orderSearchRequest: OrderSearchRequest, successHandler: (SearchOrderResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun stopReservation(reservationStopRequest: ReservationStopRequest, successHandler: (StopReservationResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getDailySummery(dailySumRequest: DailySumRequest, successHandler: (DailySummResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun addGbUser(addUserRequest: AddUserRequest, successHandler: (AddUserResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getGbUser(usersRequest: UsersRequest, successHandler: (UserListReponse) -> Unit, failureHandler: (String?) -> Unit)
    fun rmGbUser(rmUserRequest: RmUserRequest, successHandler: (RmUserResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun editGbUser(editUserRequest: EditUserRequest, successHandler: (EditUserResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getDiscounts(discountRequest: DiscountRequest, successHandler: (GetDiscountResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun addDiscounts(addDiscountRequest: AddDiscountRequest, successHandler: (AddDiscountResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun rmDiscounts(rmDiscountRequest: RmDiscountRequest, successHandler: (RmDiscountResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun editDiscounts(editDiscountRequest: EditDiscountRequest, successHandler: (EditDiscountResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun logout(logoutRequest: LogoutRequest, successHandler: (LogoutResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun forgotPass(forgotPassRequest: ForgotPassRequest, successHandler: (ForgotPassResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun getBankDetail(bankDetailRequest: BankDetailRequest, successHandler: (GetBankDetailResponse) -> Unit, failureHandler: (String?) -> Unit)
    fun addBankDetail(addBankDetailRequest: AddBankDetailRequest, successHandler: (AddBankDetailResponse) -> Unit, failureHandler: (String?) -> Unit)
 fun getRestaurantStatus(statusRequest: StatusRequest, successHandler: (ResturantStatusResponse) -> Unit, failureHandler: (String?) -> Unit)
}