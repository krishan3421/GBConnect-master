package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.bank.AddBankDetailRequest
import com.gb.restaurant.model.bank.AddBankDetailResponse
import com.gb.restaurant.model.bank.BankDetailRequest
import com.gb.restaurant.model.bank.GetBankDetailResponse
import com.gb.restaurant.model.discount.adddiscount.AddDiscountResponse
import com.gb.restaurant.model.discount.editdiscount.EditDiscountRequest
import com.gb.restaurant.model.discount.getdiscount.Discount
import com.gb.restaurant.model.discount.getdiscount.DiscountRequest
import com.gb.restaurant.model.discount.rmdiscount.RmDiscountRequest
import com.gb.restaurant.model.discount.rmdiscount.RmDiscountResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.rslogin.RsLoginRq
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.BankViewModel
import com.gb.restaurant.viewmodel.CouponViewModel
import kotlinx.android.synthetic.main.activity_bank_info.*
import kotlinx.android.synthetic.main.activity_restaurant_login.*
import kotlinx.android.synthetic.main.content_bank_info.*
import kotlinx.android.synthetic.main.content_restaurant_login.*
import kotlinx.android.synthetic.main.custom_appbar.*

class BankInfoActivity : BaseActivity() ,View.OnClickListener{
    companion object{
        val TAG:String = BankInfoActivity::class.java.simpleName
    }
    private lateinit var viewModel: BankViewModel
    var rsLoginResponse: RsLoginResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        setContentView(R.layout.activity_bank_info)
        initData()
        initView()
    }

    private fun statusBarTransparent(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
    fun backClick(v: View){
        onBackPressed()
    }


    private fun initData(){
        try{
            rsLoginResponse = MyApp.instance.rsLoginResponse
            viewModel = createViewModel()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    private fun initView(){
        try{
            add_update_button.setOnClickListener(this)
            attachObserver()
            back_layout.setOnClickListener {
                onBackPressed()
            }

            callGetBankDetailService()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
private fun BankValidation(){
    try{
        if(!Validation.isOnline(this)){
            Util.alert(getString(R.string.internet_connected),this)
            showSnackBar(progressbar,getString(R.string.internet_connected))
            return
        }else if(bank_name_text.text.toString().isEmpty()){
            Util.alert(getString(R.string.bank_name_empty),this)
            return
        }else if(holder_name_text.text.toString().isEmpty()){
            Util.alert(getString(R.string.account_holder_empty),this)
            return
        }
        else if(account_number_text.text.toString().isEmpty()){
            Util.alert(getString(R.string.account_number_empty),this)
            return
        }else if(account_number_text.text.toString().length <Constant.BANK.ACCOUNT_MIN_LENGTH || account_number_text.text.toString().length >Constant.BANK.ACCOUNT_MAX_LENGTH){
            Util.alert(getString(R.string.account_number_length),this)
            return
        }else if(routing_text.text.toString().isEmpty()){
            Util.alert(getString(R.string.routing_empty),this)
            return
        }else if(routing_text.text.toString().length !=Constant.BANK.ROUTING_LENGTH){
            Util.alert(getString(R.string.routing_length),this)
            return
        }else {
            var addBankDetailRequest=AddBankDetailRequest()
            addBankDetailRequest.bank= bank_name_text.text.toString()
            addBankDetailRequest.name=holder_name_text.text.toString()
            addBankDetailRequest.routing=routing_text.text.toString()
            addBankDetailRequest.account=account_number_text.text.toString()
            callAddBankService(addBankDetailRequest)
        }
    }catch (e:Exception){
        e.printStackTrace()
        Log.e(TAG,e.message!!)
    }
}
    private fun callAddBankService(addBankDetailRequest: AddBankDetailRequest){
        try{
            addBankDetailRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
            addBankDetailRequest.deviceversion = Util.getVersionName(this)
            viewModel.addBankDetail(addBankDetailRequest)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    override fun onResume() {
        super.onResume()
        try{
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    private fun callGetBankDetailService(){
        try{
            if(Validation.isOnline(this)){
                var bankDetailRequest=BankDetailRequest()
                bankDetailRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                bankDetailRequest.deviceversion = Util.getVersionName(this)
               // println("activeresponse>>> ${Util.getStringFromBean(discountRequest)}")
                viewModel.getBankDetail(bankDetailRequest)
            }else{
                showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showSnackBar(progress_bar,it) }
        })

        viewModel.getBankResponse.observe(this, Observer<GetBankDetailResponse> {
            it?.let {
                if(it.status !=Constant.STATUS.FAIL){

                }else{
                   // Util.alert("${it.result?:""}",this)
                }
                Util.alert("${it.result?:""}",this)
            }
        })

        viewModel.addBankResponse.observe(this, Observer<AddBankDetailResponse> {
            it?.let {
                if(it.status ==Constant.STATUS.FAIL){
                    Util.alert("${it.result?:""}",this)
                }else{
                    Util.alert("${it.data?.message?:""}.",this)
                }


            }
        })


    }


    private fun createViewModel(): BankViewModel =
        ViewModelProviders.of(this).get(BankViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

    override fun onClick(view: View?) {
        when(view){
            add_update_button->{
                BankValidation()
            }
        }
    }
}
