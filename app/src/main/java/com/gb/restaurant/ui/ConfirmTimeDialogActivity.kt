package com.gb.restaurant.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.ActivityConfirmTimeDialogBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.OrderStatusViewModel


class ConfirmTimeDialogActivity : FragmentActivity() ,View.OnClickListener{

    private lateinit var viewModel: OrderStatusViewModel
    var rsLoginResponse: RsLoginResponse? = null
    private var orderId:String = ""
    private var orderType:String = "Delivery"
    private var hold:String = ""
    private var timeList:MutableList<String> = mutableListOf<String>()
    private lateinit var binding: ActivityConfirmTimeDialogBinding
    companion object{
        private val TAG:String = ConfirmTimeDialogActivity::class.java.simpleName
        public val ORDER_ID:String = "ORDER_ID"
        public val TYPE:String = "TYPE"
        public val HOLD:String = "HOLD"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_confirm_time_dialog)
        binding = ActivityConfirmTimeDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
             binding.apply {
                 cancel.setOnClickListener(this@ConfirmTimeDialogActivity)
                 confirm.setOnClickListener(this@ConfirmTimeDialogActivity)
                 val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this@ConfirmTimeDialogActivity, R.layout.time_item, timeList)
                 mobileList.adapter = adapter
                 if(hold.equals("Yes",true)){
                     mobileList.visibility=View.GONE
                     confirm.visibility=View.VISIBLE
                     clickOnButtonTime.text = "Click on Button To Confirm"
                 }else{
                     mobileList.visibility=View.VISIBLE
                     confirm.visibility=View.GONE
                 }
                 mobileList.setOnItemClickListener { adapterView, view, position, l ->
                     var orderStatusRequest = OrderStatusRequest()
                     orderStatusRequest.deviceversion = Util.getVersionName(this@ConfirmTimeDialogActivity)
                     orderStatusRequest.status = Constant.ORDER_STATUS.CONFIRMED
                     orderStatusRequest.order_id = orderId
                     if(orderType.equals("Delivery",true)) {
                         orderStatusRequest.readytime = "${rsLoginResponse?.data?.deliverytime?.get(position)} minutes"
                     }else{
                         orderStatusRequest.readytime = "${rsLoginResponse?.data?.pickuptime?.get(position)} minutes"
                     }
                     callService(orderStatusRequest)
                 }
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
                binding.cancel->{
                    //cancelStatusPopup()
                    orderStatusRequest.status = Constant.ORDER_STATUS.CANCEL
                }
                binding.confirm->{
                    orderStatusRequest.status = Constant.ORDER_STATUS.CONFIRMED

                }
            }
            callService(orderStatusRequest)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }

    }

    private fun cancelStatusPopup(){
        MaterialDialog(this).show {
            setTheme(R.style.AppThemeMD)
            title(R.string.choose_reason)
            listItemsSingleChoice(
                R.array.cancel_reason_list, initialSelection = 1
            ) { _, index, text ->
                var orderStatusRequest = OrderStatusRequest()
                orderStatusRequest.details=text
                callService(orderStatusRequest)
            }
            positiveButton(R.string.choose)
        }
    }


    private fun callService(orderStatusRequest: OrderStatusRequest){
        try{
            if(Validation.isOnline(this)){
                orderStatusRequest.deviceversion = Util.getVersionName(this)
                orderStatusRequest.order_id = orderId
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
               if(it.status == Constant.STATUS.FAIL){
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
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

    private fun showToast(msg:String){
        try{
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}
