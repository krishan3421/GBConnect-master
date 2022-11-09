package com.gb.restaurant.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.OrderStatusViewModel
import kotlinx.android.synthetic.main.activity_confirm_time_dialog.*


class ConfirmTimeDialogActivity : FragmentActivity() ,View.OnClickListener{

    private lateinit var viewModel: OrderStatusViewModel
    var rsLoginResponse: RsLoginResponse? = null
    private var orderId:String = ""
    private var orderType:String = "Delivery"
    private var hold:String = ""
    private var timeList:MutableList<String> = mutableListOf<String>()
    companion object{
        private val TAG:String = ConfirmTimeDialogActivity::class.java.simpleName
        public val ORDER_ID:String = "ORDER_ID"
        public val TYPE:String = "TYPE"
        public val HOLD:String = "HOLD"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_time_dialog)
        initData()
        initView()
    }

    private fun initData(){
        try{
            viewModel = createViewModel()
            rsLoginResponse = MyApp.instance.rsLoginResponse
            intent.apply {
                orderId = this.getStringExtra(ORDER_ID)!!
                orderType =  this.getStringExtra(TYPE)!!
                hold =  this.getStringExtra(HOLD)!!
            }
            rsLoginResponse?.data?.let {
                if(!hold.equals("Yes",true)) {
                    if (orderType.equals("Delivery", true)) {
                        if (!it.deliverytime.isNullOrEmpty()) {
                            it.deliverytime?.forEachIndexed { index, s ->
                                timeList.add("READY IN $s MIN")
                            }
                        }
                    }else {
                        if (!it.pickuptime.isNullOrEmpty()) {
                            it.pickuptime?.forEachIndexed { index, s ->
                                timeList.add("READY IN $s MIN")
                            }
                        }
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

     private fun initView(){
         try{
             attachObserver()
             cancel.setOnClickListener(this)
             confirm.setOnClickListener(this)
             val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.time_item, timeList)
             mobile_list.adapter = adapter
             if(hold.equals("Yes",true)){
                 mobile_list.visibility=View.GONE
                 confirm.visibility=View.VISIBLE
                 click_on_button_time.text = "Click on Button To Confirm"
             }else{
                 mobile_list.visibility=View.VISIBLE
                 confirm.visibility=View.GONE
             }
             mobile_list.setOnItemClickListener { adapterView, view, position, l ->
                 var orderStatusRequest = OrderStatusRequest()
                 orderStatusRequest.deviceversion = Util.getVersionName(this)
                 orderStatusRequest.status = Constant.ORDER_STATUS.CONFIRMED
                 orderStatusRequest.order_id = orderId
                 if(orderType.equals("Delivery",true)) {
                     orderStatusRequest.readytime = "${rsLoginResponse?.data?.deliverytime?.get(position)} minutes"
                 }else{
                     orderStatusRequest.readytime = "${rsLoginResponse?.data?.pickuptime?.get(position)} minutes"
                 }
                 callService(orderStatusRequest)
             }
         }catch (e:Exception){
             e.printStackTrace()
             Log.e(TAG,e.message!!)
         }
     }

    override fun onClick(view: View?) {
        try{
            var orderStatusRequest = OrderStatusRequest()
            when(view){
                cancel->{
                    orderStatusRequest.status = Constant.ORDER_STATUS.CANCEL
                }
                confirm->{
                    orderStatusRequest.status = Constant.ORDER_STATUS.CONFIRMED
                }
            }

           orderStatusRequest.deviceversion = Util.getVersionName(this)
            orderStatusRequest.order_id = orderId

            callService(orderStatusRequest)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }

    }


    private fun callService(orderStatusRequest: OrderStatusRequest){
        try{
            if(Validation.isOnline(this)){
                orderStatusRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                println("request>>>>> ${Util.getStringFromBean(orderStatusRequest)}")
                viewModel.orderStatus(orderStatusRequest)
            }else{
                showToast(getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun closePage(view: View){
        onBackPressed()
    }


    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showToast(it) }
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

    private fun createViewModel(): OrderStatusViewModel =
        ViewModelProviders.of(this).get(OrderStatusViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

    private fun showToast(msg:String){
        try{
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}
