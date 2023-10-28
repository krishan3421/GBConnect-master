package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.ActivityResetPassBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.resetpass.ResetPassRequest
import com.gb.restaurant.model.resetpass.ResetPassResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ResetPassViewModel
import com.gb.restaurant.session.SessionManager

class ResetPassActivity : BaseActivity() {

    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: ResetPassViewModel
    private lateinit var binding: ActivityResetPassBinding
    companion object{
        private val TAG = ResetPassActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_reset_pass)
        binding = ActivityResetPassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()

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
             setSupportActionBar(binding.toolbar)
             binding.toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
             binding.toolbar.setNavigationOnClickListener { onBackPressed() }

             attachObserver()
         }catch (e:Exception){
             e.printStackTrace()
             Log.e(TAG,e.message!!)
         }
     }

     fun submitMethod(v :View){
        try{
            callService()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun callService(){
        try{
            if(!Validation.isOnline(this)){
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
                return
            }else if(binding.contentResetPass.newPassText.text.isNullOrEmpty()){
                showSnackBar(binding.progressBar,getString(R.string.new_pass_empty))
                return

            }else if(!binding.contentResetPass.conPassText.text.toString().equals(binding.contentResetPass.newPassText.text.toString())){
                showSnackBar(binding.progressBar,getString(R.string.con_pass_empty))
                return
            }else{
                var resetPassRequest=ResetPassRequest()
                resetPassRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                resetPassRequest.user_id = rsLoginResponse?.data?.loginId!!
                resetPassRequest.password = binding.contentResetPass.newPassText.text.toString()
                resetPassRequest.deviceversion = Util.getVersionName(this)
               // println("data>>>>>> ${Util.getStringFromBean(resetPassRequest)}")
                viewModel.resetPass(resetPassRequest)
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
        viewModel.resetPassResponse.observe(this, Observer<ResetPassResponse> {
            it?.let {
                if(it.status != Constant.STATUS.FAIL){
                    if(it.data !=null) {
                        var sessionManager = SessionManager(this)
                        sessionManager.updatePassword(it.data.password?:"")
                    }
                    successAlert(it.result?:"")
                }else{
                    Util.alert(it.result?:"",this)
                }

            }
        })


    }

    fun successAlert(message:String){
        try {
            MaterialDialog(this).show {
                title(R.string.app_name_alert)
                message(null,message)
                positiveButton {
                    this@ResetPassActivity.onBackPressed()
                }
                positiveButton(R.string.ok)

            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(Util.TAG,e.message!!)
        }
    }
    private fun createViewModel(): ResetPassViewModel =
        ViewModelProviders.of(this).get(ResetPassViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }
}
