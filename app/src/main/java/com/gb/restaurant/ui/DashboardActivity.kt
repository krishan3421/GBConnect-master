package com.gb.restaurant.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.enumm.Login
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.rslogin.RsLoginRq
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.RsLoginViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.grabull.session.SessionManager

import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.toolbar
import kotlinx.android.synthetic.main.activity_restaurant_login.*
import kotlinx.android.synthetic.main.content_dashboard.*
import kotlinx.android.synthetic.main.content_restaurant_login.*

class DashboardActivity : BaseActivity() {
    private var firbaseToken :String? = null
    private lateinit var viewModel: RsLoginViewModel
    private var userName:String?=null
    companion object{
        private val TAG:String = DashboardActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(toolbar)
        token()
        toolbar.apply { visibility =View.GONE }
        initView()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun initView(){
        try{
            viewModel = createViewModel()
            attachObserver()
            var sessionManager=SessionManager(this)
            if(sessionManager.isAutoLoggedIn()){
                button_rest.visibility=View.GONE
                var userDetail=sessionManager.getUserDetails()
                userName = userDetail[SessionManager.USERNAME]
                var password = userDetail[SessionManager.PASSWORD]
                loginMethod(userName?:"",password?:"")
            }else{
                button_rest.visibility=View.VISIBLE
            }


        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun loginMethod(userName:String,password:String){
        try{
            //callHomePage()
            if(!Validation.isOnline(this)){
                Util.alert(getString(R.string.internet_connected),this)
                return
            }else if(userName.isEmpty()){
                //showSnackBar(progressbar,getString(R.string.username_empty))
                return
            }else if(password.isEmpty()){
               // showSnackBar(progressbar,getString(R.string.password_empty))
                return
            }else {
                var rsLoginRq = RsLoginRq()
                rsLoginRq.login_id =userName
                rsLoginRq.login_pw =password
                rsLoginRq.deviceversion = Util.getVersionName(this)
                viewModel.getLoginResponse(rsLoginRq)
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
            // it?.let { showSnackBar(progressbar,getString(R.string.service_error)) }
            // it?.let { println("error>>>>> ${Util.getStringFromBean(it)}") }
            //Util.alert(getString(R.string.service_error),this)
            button_rest.visibility=View.VISIBLE
            forRestaurantClick(button_rest)
        })
        viewModel.loginResponse.observe(this, Observer<RsLoginResponse> {
            it?.let {
                // println("response>>>>> ${Util.getStringFromBean(it)}")
                if(it.status == Constant.STATUS.FAIL){
                    // showSnackBar(progressbar,it.result!!)
                    //Util.alert(it.result?:"",this)
                    button_rest.visibility=View.VISIBLE
                    forRestaurantClick(button_rest)
                    //callHomePage()
                }else{
                    MyApp.instance.rsLoginResponse = it
                    MyApp.instance.rsLoginResponse?.data?.loginId=userName
                    callHomePage()
                }

            }

        })

    }
    private fun callHomePage(){
        try{
            var  intent = Intent(applicationContext, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

     fun forRestaurantClick(v:View){
        try{
            var  intent = Intent(applicationContext, RestaurantLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(RestaurantLoginActivity.LOGIN_TYPE,Login.RESTAURANT.toString())
            startActivity(intent)
            //finish()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
     fun forDriverClick(v:View){
        try{
           /* var  intent = Intent(applicationContext, DriverLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)*/
            var  intent = Intent(applicationContext, RestaurantLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(RestaurantLoginActivity.LOGIN_TYPE,Login.DRIVER.toString())
            startActivity(intent)
           // finish()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun token(){
        try{
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    firbaseToken = task.result?.token
                    MyApp.instance.deviceToken = firbaseToken?:""
                    // Log and toast
                    //val msg = getString(R.string.msg_token_fmt, token)
                    Log.d("token>>>>>>>>>", firbaseToken!!)
                    //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                })
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun createViewModel(): RsLoginViewModel =
        ViewModelProviders.of(this).get(RsLoginViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

}
