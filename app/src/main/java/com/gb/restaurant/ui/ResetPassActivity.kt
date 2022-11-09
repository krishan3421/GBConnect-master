package com.gb.restaurant.ui

import android.content.Context
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
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.resetpass.ResetPassRequest
import com.gb.restaurant.model.resetpass.ResetPassResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ResetPassViewModel
import com.grabull.session.SessionManager

import kotlinx.android.synthetic.main.activity_reset_pass.*
import kotlinx.android.synthetic.main.content_reset_pass.*

class ResetPassActivity : BaseActivity() {

    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: ResetPassViewModel
    companion object{
        private val TAG = ResetPassActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pass)
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
             setSupportActionBar(toolbar)
             toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
             toolbar.setNavigationOnClickListener { onBackPressed() }

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
                showSnackBar(progress_bar,getString(R.string.internet_connected))
                return
            }else if(new_pass_text.text.isNullOrEmpty()){
                showSnackBar(progress_bar,getString(R.string.new_pass_empty))
                return

            }else if(!con_pass_text.text.toString().equals(new_pass_text.text.toString())){
                showSnackBar(progress_bar,getString(R.string.con_pass_empty))
                return
            }else{
                var resetPassRequest=ResetPassRequest()
                resetPassRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                resetPassRequest.user_id = rsLoginResponse?.data?.loginId!!
                resetPassRequest.password = new_pass_text.text.toString()
                resetPassRequest.deviceversion = Util.getVersionName(this)
                println("data>>>>>> ${Util.getStringFromBean(resetPassRequest)}")
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
            it?.let { showSnackBar(progress_bar,it) }
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
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }
}
