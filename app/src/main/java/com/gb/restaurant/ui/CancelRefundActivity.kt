package com.gb.restaurant.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.orderdetail.OrderDetailRequest
import com.gb.restaurant.model.orderdetail.OrderDetailResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.TipsViewModel
import kotlinx.android.synthetic.main.activity_cancel_refund.*
import kotlinx.android.synthetic.main.content_cancel_refund.*
import kotlinx.android.synthetic.main.custom_appbar.*

class CancelRefundActivity : BaseActivity() {

    private lateinit var viewModel: TipsViewModel
    private var orderDetailResponse:OrderDetailResponse?=null
    companion object{
        val TAG = CancelRefundActivity::class.java.simpleName
    }
    var rsLoginResponse: RsLoginResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        setContentView(R.layout.activity_cancel_refund)
        initData()
        initView()

    }

    private fun initData() {
        try{
            rsLoginResponse = MyApp.instance.rsLoginResponse

            viewModel = createViewModel()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun statusBarTransparent(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
       /* if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }*/
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    private fun initView(){
        try{
            back_layout.setOnClickListener {
                onBackPressed()
            }
            attachObserver()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

//    private fun callService(){
//        try{
//            if(Validation.isOnline(this)){
//                var activeOrderRequest = ActiveOrderRequest()
//                activeOrderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
//               // println("request date>>>>>> ${Util.getStringFromBean(reportRequest)}")
//                viewModel.getActiveOrderList(activeOrderRequest)
//            }else{
//                showSnackBar(progress_bar,getString(R.string.internet_connected))
//            }
//        }catch (e:Exception){
//            e.printStackTrace()
//            Log.e(TAG,e.message)
//        }
//    }
 fun searchTips(v:View){
     try{
         //2005151127531303503414
         callOrderSearchService()
     }catch (e:Exception){
         e.printStackTrace()
         Log.e(TAG,e.message!!)
     }
 }
    private fun callOrderSearchService(){
        try{
            if(!Validation.isOnline(this)){
                showSnackBar(progress_bar,getString(R.string.internet_connected))
                return
            }else if(search_edittext.text.toString().isNullOrEmpty()){
                showSnackBar(progress_bar,"Please provide valid Order-Id")
                return
            }else{

                var orderDetailRequest = OrderDetailRequest()
                orderDetailRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderDetailRequest.order_id = search_edittext.text.toString().trim()
                orderDetailRequest.deviceversion = Util.getVersionName(this)
                // println("request date>>>>>> ${Util.getStringFromBean(reportRequest)}")
                viewModel.getOrderDetailResponse(orderDetailRequest)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let {
                showSnackBar(progress_bar,it)
            }
            main_layout.visibility =View.GONE
            order_not_found_text.visibility=View.VISIBLE
        })
        viewModel.orderDetailResponse.observe(this, Observer<OrderDetailResponse> {
            it?.let {
                orderDetailResponse = it
                    if(it.status?:""=="success"){
                          if(it.result!!.contains("Not Available")){
                              main_layout.visibility =View.GONE
                              order_not_found_text.visibility=View.VISIBLE
                          }else{
                              main_layout.visibility =View.VISIBLE
                              order_not_found_text.visibility=View.GONE
                              setDetailData(it)
                          }
                    }else{
                        main_layout.visibility =View.GONE
                        order_not_found_text.visibility=View.VISIBLE
                    }
            }
        })
        viewModel.orderStatusResponse.observe(this, Observer<OrderStatusResponse> {
            it?.let {
                if(it.status ==Constant.STATUS.FAIL){
                    showToast(it.result!!)
                }else{
                    showToast(it.result!!)
                    finishPage()
                }
            }
        })


    }

    private fun finishPage(){
        try{
            val intent = Intent()
            setResult(Activity.RESULT_OK,intent)
            onBackPressed()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private fun setDetailData(orderDetail:OrderDetailResponse){
        try {
            var data = orderDetail.data

            data?.let {
                var date2 = it.date2
                var dateArray = date2?.split("\\s".toRegex())
                var month = dateArray?.get(0) ?:""
                var day = dateArray?.get(1) ?:""
                mothText.text = month
                dateText.text = day
                nameText.text = it.name
                add_text.text=it.delivery
                idText.text = it.id
                statusText.text = it.type
                delivery_text.text = it.status
                date_text.text = it.date2
                price_text.text = "$${it.total}"
                if(it.payment.equals("Paid",true)){
                    refund_button.visibility = View.VISIBLE
                    partial_layout.visibility = View.VISIBLE
                }else{
                    refund_button.visibility = View.GONE
                    partial_layout.visibility = View.GONE
                }
                //refund_button
            }

        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun cancelMethod(v:View){
        try{
               if(!Validation.isOnline(this)){
                   showToast(getString(R.string.internet_connected))
                   return
               }else{
                   var orderStatusRequest = OrderStatusRequest()
                   orderStatusRequest.status = Constant.ORDER_STATUS.CANCEL
                   orderStatusRequest.order_id = orderDetailResponse?.data?.id?:""
                   orderStatusRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                   orderStatusRequest.details = message_text.text.toString()?:""
                   orderStatusRequest.deviceversion = Util.getVersionName(this)
                   println("request>>>>> ${Util.getStringFromBean(orderStatusRequest)}")
                   viewModel.orderStatus(orderStatusRequest)
               }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    fun refundMethod(v:View){
        try{
            if(!Validation.isOnline(this)){
                showToast(getString(R.string.internet_connected))
                return
            }else{
                var orderStatusRequest = OrderStatusRequest()
                orderStatusRequest.order_id = orderDetailResponse?.data?.id?:""
                orderStatusRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderStatusRequest.deviceversion = Util.getVersionName(this)
                if(v==partial_refund_button){
                    orderStatusRequest.status =  Constant.ORDER_STATUS.PARTIAL_REFUND
                    if(partial_refund_edittext.text.isNullOrEmpty()){
                        Util.alert("Please add Partial Amount.",this)
                        return
                    }
                    orderStatusRequest.refund=partial_refund_edittext.text.toString()
                }else{
                    orderStatusRequest.status = Constant.ORDER_STATUS.REFUND
                }
                println("request>>>>> ${Util.getStringFromBean(orderStatusRequest)}")
                viewModel.orderStatus(orderStatusRequest)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun createViewModel(): TipsViewModel =
        ViewModelProviders.of(this).get(TipsViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

}
