package com.gb.restaurant.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.enumm.Login
import com.gb.restaurant.model.forgot.ForgotPassRequest
import com.gb.restaurant.model.forgot.ForgotPassResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.rslogin.RsLoginRq
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.RsLoginViewModel
import com.grabull.session.SessionManager
import kotlinx.android.synthetic.main.activity_restaurant_login.*
import kotlinx.android.synthetic.main.content_restaurant_login.*
import kotlinx.android.synthetic.main.forgot_pass_layout.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class RestaurantLoginActivity : BaseActivity() {

    private lateinit var viewModel: RsLoginViewModel
    private var loginType:String = Login.RESTAURANT.toString()
    private var forgotPopUp:MaterialDialog?=null
    companion object{
     private val TAG = RestaurantLoginActivity::class.java.simpleName
         val LOGIN_TYPE = "LOGIN_TYPE"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_login)
        setSupportActionBar(toolbar)
        initData()
        initView()

    }

    private fun initData() {
        try {
           intent.apply {
               loginType = this.getStringExtra(LOGIN_TYPE)!!
           }
            println("data>>>>>>>${loginType}")
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initView(){
        try{
            toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back_black)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            toolbar.title = getString(R.string.back)
            toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.colorAccent))
            viewModel = createViewModel()
            attachObserver()
            if(loginType== Login.DRIVER.toString()){
                login_button.text = "LOGIN"
                login_title.text = "Start Earning $"
                footer_title.text = "Drive for Grabull"
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
    fun loginMethod(v:View){
        try{
            //callHomePage()
        if(!Validation.isOnline(this)){
                showSnackBar(progressbar,getString(R.string.internet_connected))
                return
            }else if(user_id_text.text.toString().isEmpty()){
                showSnackBar(progressbar,getString(R.string.username_empty))
                return
            }else if(password_text.text.toString().isEmpty()){
                showSnackBar(progressbar,getString(R.string.password_empty))
                return
            }else {
                var rsLoginRq = RsLoginRq()
                rsLoginRq.login_id = user_id_text.text.toString()
                rsLoginRq.login_pw =password_text.text.toString()
                rsLoginRq.deviceversion = Util.getVersionName(this)
               viewModel.getLoginResponse(rsLoginRq)

            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun signUpMethod(view:View){
        try{
            var  intent = Intent(applicationContext, RegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun forgotPassClick(view:View){
        try{
            openForgotPassPopUp()
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
           // it?.let { showSnackBar(progressbar,getString(R.string.service_error)) }
           // it?.let { println("error>>>>> ${Util.getStringFromBean(it)}") }
            Util.alert(getString(R.string.service_error),this)
            forgotPopUp?.popup_progress?.visibility=View.GONE
        })
        viewModel.loginResponse.observe(this, Observer<RsLoginResponse> {
            it?.let {
               // println("response>>>>> ${Util.getStringFromBean(it)}")
                if(it.status ==Constant.STATUS.FAIL){
                   // showSnackBar(progressbar,it.result!!)
                    Util.alert(it.result?:"",this)
                    //callHomePage()
                }else{
                    MyApp.instance.rsLoginResponse = it
                    MyApp.instance.rsLoginResponse?.data?.loginId=user_id_text.text.toString()
                    var securityManager=SessionManager(this)
                    securityManager.createLoginSession(user_id_text.text.toString().trim(),password_text.text.toString().trim(),auto_login_checkbox.isChecked)
                    callHomePage()
                }

            }

        })
        viewModel.forgotResponse.observe(this, Observer<ForgotPassResponse> {
           it?.let {
               forgotPopUp?.popup_progress?.visibility=View.GONE
               if(it.status !=Constant.STATUS.FAIL){
                   forgotPopUp?.dismiss()
               }
               Util.alert(it.result?:"",this@RestaurantLoginActivity)
           }
        })

    }

    private fun openForgotPassPopUp(){
        try {
            MaterialDialog(this, MaterialDialog.DEFAULT_BEHAVIOR).show {
                title(R.string.app_name_alert)
                noAutoDismiss()
                message(R.string.forgot_password_text)
                customView(
                    R.layout.forgot_pass_layout,
                    scrollable = true,
                    horizontalPadding = true
                )
                positiveButton {
                    if (!Validation.isOnline(this@RestaurantLoginActivity)) {
                        Util.alert(getString(R.string.internet_connected), this@RestaurantLoginActivity)
                    } else if (email_forgot_text.text.toString().isEmpty()) {
                        Util.alert(getString(R.string.forgot_pass_validation),this@RestaurantLoginActivity)
                    } else {
                        popup_progress.visibility = View.VISIBLE
                        forgotPopUp = this
                        callForgotPassService(email_forgot_text.text.toString().trim())
                    }
                }
                negativeButton {
                    dismiss()
                }
                positiveButton(R.string.ok)
                negativeButton(android.R.string.cancel)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun callForgotPassService(text:String){
        try{
            //progressbar.visibility = View.VISIBLE
            var forgotPassRequest=ForgotPassRequest()
            forgotPassRequest.user_id=text
            forgotPassRequest.deviceversion = Util.getVersionName(this)
           viewModel.forgotPass(forgotPassRequest)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun callHomePage(){
        try{
            var  intent = Intent(applicationContext, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    private fun createViewModel(): RsLoginViewModel =
        ViewModelProviders.of(this).get(RsLoginViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progressbar.visibility = View.VISIBLE else progressbar.visibility = View.GONE
    }

}
