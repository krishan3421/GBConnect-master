package com.gb.restaurant.ui

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
import com.gb.restaurant.databinding.ActivityAddTipsBinding
import com.gb.restaurant.databinding.ActivityBankInfoBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.bank.AddBankDetailRequest
import com.gb.restaurant.model.bank.AddBankDetailResponse
import com.gb.restaurant.model.bank.BankDetailRequest
import com.gb.restaurant.model.bank.GetBankDetailResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.BankViewModel

class BankInfoActivity : BaseActivity() ,View.OnClickListener{
    companion object{
        val TAG:String = BankInfoActivity::class.java.simpleName
    }
    private lateinit var viewModel: BankViewModel
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var binding: ActivityBankInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        //setContentView(R.layout.activity_bank_info)
        binding = ActivityBankInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            binding.contentBankInfo.addUpdateButton.setOnClickListener(this)
            attachObserver()
            binding.customAppbar.backLayout.setOnClickListener {
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
            showSnackBar(binding.progressBar,getString(R.string.internet_connected))
            return
        }else if(binding.contentBankInfo.bankNameText.text.toString().isEmpty()){
            Util.alert(getString(R.string.bank_name_empty),this)
            return
        }else if(binding.contentBankInfo.holderNameText.text.toString().isEmpty()){
            Util.alert(getString(R.string.account_holder_empty),this)
            return
        }
        else if(binding.contentBankInfo.accountNumberText.text.toString().isEmpty()){
            Util.alert(getString(R.string.account_number_empty),this)
            return
        }else if(binding.contentBankInfo.accountNumberText.text.toString().length < Constant.BANK.ACCOUNT_MIN_LENGTH || binding.contentBankInfo.accountNumberText.text.toString().length > Constant.BANK.ACCOUNT_MAX_LENGTH){
            Util.alert(getString(R.string.account_number_length),this)
            return
        }else if(binding.contentBankInfo.routingText.text.toString().isEmpty()){
            Util.alert(getString(R.string.routing_empty),this)
            return
        }else if(binding.contentBankInfo.routingText.text.toString().length != Constant.BANK.ROUTING_LENGTH){
            Util.alert(getString(R.string.routing_length),this)
            return
        }else {
            var addBankDetailRequest=AddBankDetailRequest()
            addBankDetailRequest.bank= binding.contentBankInfo.bankNameText.text.toString()
            addBankDetailRequest.name=binding.contentBankInfo.holderNameText.text.toString()
            addBankDetailRequest.routing=binding.contentBankInfo.routingText.text.toString()
            addBankDetailRequest.account=binding.contentBankInfo.accountNumberText.text.toString()
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
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
            it?.let { showSnackBar(binding.progressBar,it) }
        })

        viewModel.getBankResponse.observe(this, Observer<GetBankDetailResponse> {
            it?.let {
                if(it.status != Constant.STATUS.FAIL){

                }else{
                   // Util.alert("${it.result?:""}",this)
                }
                Util.alert("${it.result?:""}",this)
            }
        })

        viewModel.addBankResponse.observe(this, Observer<AddBankDetailResponse> {
            it?.let {
                if(it.status == Constant.STATUS.FAIL){
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
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

    override fun onClick(view: View?) {
        when(view){
            binding.contentBankInfo.addUpdateButton->{
                BankValidation()
            }
        }
    }
}
