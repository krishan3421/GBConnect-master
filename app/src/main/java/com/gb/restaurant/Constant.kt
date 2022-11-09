package com.gb.restaurant
//https://www.grabull.com/web-api/
//admin/api_gb@2019
class Constant {

    companion object{
        //const val URL ="https://www.grabull.com.au"
        const val URL ="https://www.grabull.com"
    }
    interface WEBSERVICE{
          companion object{
              const val RESTSERVICEURL = "$URL/web-api/"
              const val API_ID = "GB19AP01"
              const val API_KEY = "ba6ee13256e5f0d75eacbf87de167541"
          }
    }

    interface SERVICE_API{
        companion object{
            const val RESTAURANT_LOGIN = "restaurant-connect/api.php"
            const val GET_ALL_ORDER = "restaurant-connect/api.php"
            const val COMMON_URL = "restaurant-connect/api.php"
            const val ORDER_STATUS = "restaurant-connect/order-status.php"
            const val ADD_ORDER_TIPS = "restaurant-connect/api.php"
            const val ADD_ORDER_ITEMS = "restaurant-connect/api.php"
            const val RESERVATION_STATUS = "restaurant-connect/reservation-status.php"
            const val ENQUIRY_STATUS = "restaurant-connect/enquiry-status.php"

        }
    }

    interface SERVICE_TYPE{
        companion object{
            const val GET_NEW_ORDER = "GetNewOrder"
            const val GET_HOLD_ORDER = "GetHoldOrder"
            const val GET_ACTIVE_ORDER = "GetActiveOrder"
            const val GET_ALL_ORDER = "GetAllOrder"
            const val GET_REOPRT = "GetReport"
            const val GET_CALLBACK = "GetCallback"
            const val GET_STOP_TODAY = "GetStoptoday"
            const val GET_OPEN_TODAY = "GetOpentoday"
            const val GET_UPDATE_SETTING = "UpdateSetting"
            const val GET_ORDER_DETAILS = "GetOrderDetails"
            const val RESETPASSWORD = "ResetPassword"
            const val GET_RESERVATION = "GetReservation"
            const val GET_RESERVATION_STOP = "GetReservationStop"
            const val GET_ENQUIRY = "GetEnquiry"
            const val GET_RESERVATION_STATUS = "GetReservationStatus"
            const val ENQUIRY_STATUS = "EnquiryStatus"
            const val CONFIRM_ORDER = "ConfirmOrder"
            const val ADD_ORDER_ITEMS = "AddOrderItems"
            const val ADD_ORDER_TIPS = "AddOrderTips"
            const val REGISTER = "RegisterRestaurant"
            const val GET_REPORT_WEEK = "GetReportWeeks"
            const val GET_ACTIVE_ORDER_LIST = "GetActiveOrderList"
            const val GET_ORDER_SEARCH = "GetOrderSearch"
            const val GET_DAILY_SUMMERY = "GetDailySummary"
            const val ADD_GB_USER = "AddGBUser"
            const val GET_GB_USER = "GetGBUser"
            const val REMOVE_GB_USER = "removeGBUser"
            const val EDIT_GB_USER = "EditGBUser"
            const val GET_DISCOUNTS = "getDiscounts"
            const val ADD_DISCOUNTS = "addDiscounts"
            const val EDIT_DISCOUNTS = "editDiscounts"
            const val RESET_DISCOUNTS = "resetDiscounts"
            const val LOGOUT = "GetLogout"
            const val FORGOT_PASSWORD = "ForgotPassword"
            const val GET_BANK_DETAIL = "GetBankDetails"
            const val ADD_BANK_DETAIL = "AddBankDetails"
        }
    }

    interface STATUS{
        companion object{
            const val FAIL = "fail"
            const val SUCCESS = "success"
        }
    }
    interface ORDER_STATUS{
        companion object{
            const val CONFIRMED = "Confirmed"
            const val CANCEL = "Cancel"
            const val PICKEDUP = "Picked Up"
            const val DELIVERED = "Delivered"
            const val CLOSED = "Closed"
            const val REFUND = "Refund"
            const val PARTIAL_REFUND = "Partial Refund"

        }
    }
    interface RESERVATION_STOP{
        companion object{
            const val TODAY = "Today"
            const val DAY = "Day"
            const val BETWEEN = "Between"

        }
    }

    interface TAB{
        companion object{
            const val NEW = 0
            const val ACTIVE = 1
            const val COMPLETED = 2
        }
    }
    interface GB_DELIVERY{
        companion object{
            const val GB = "Yes"
            const val SELF = "No"
        }
    }
    interface DEVICE{
        companion object{
            const val DEVICE_TYPE:String = "Android"

        }
    }
    interface BANK{
        companion object{
            const val ROUTING_LENGTH:Int = 9
            const val ACCOUNT_MIN_LENGTH:Int = 3
            const val ACCOUNT_MAX_LENGTH:Int = 14
        }
    }

}