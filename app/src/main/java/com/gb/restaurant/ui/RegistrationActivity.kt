package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.gb.restaurant.Constant
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.register.RegisterRequest
import com.gb.restaurant.model.register.RegisterResponse
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.RsLoginViewModel

import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_registration.toolbar
import kotlinx.android.synthetic.main.content_registration.*

class RegistrationActivity : BaseActivity() {
    private lateinit var viewModel: RsLoginViewModel
    companion object{
        private val TAG:String = RegistrationActivity::class.java.simpleName
    }
    private  var roleIndex = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        initView()
    }

    private fun initView(){
        try{
            setSupportActionBar(toolbar)
            toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            restaurant_role_text.setText("Owner")
            viewModel = createViewModel()
            attachObserver()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun submitRegister(v:View){
        try{
            //callHomePage()
            if(!Validation.isOnline(this)){
                showSnackBar(progressbar,getString(R.string.internet_connected))
                return
            }else if(name_text.text.toString().isNullOrEmpty()){
                showSnackBar(progressbar,getString(R.string.name_empty))
                return
            }else if(!Validation.validateEmail(email_text.text.toString())){
                showSnackBar(progressbar,getString(R.string.email_empty))
                return
            }
            else if(!Validation.isValidMobile(molile_text.text.toString())){
                showSnackBar(progressbar,getString(R.string.mobile_empty))
                return
            } else if(restaurant_name_text.text.toString().isNullOrEmpty()){
                showSnackBar(progressbar,getString(R.string.resturant_name_empty))
                return
            }else if(restaurant_zip_text.text.toString().isNullOrEmpty() || restaurant_zip_text.text.toString().length <5){
                showSnackBar(progressbar,getString(R.string.zip_validation))
                return
            }
            else {
                var registerRequest =RegisterRequest()
                   registerRequest.name =name_text.text.toString()
                    registerRequest.email =email_text.text.toString()
                    registerRequest.phone=molile_text.text.toString()
                    registerRequest.rest_name =restaurant_name_text.text.toString()
                    registerRequest.rest_zip =restaurant_zip_text.text.toString()
                    registerRequest.rest_role =restaurant_role_text.text.toString()
                    registerRequest.deviceversion = Util.getVersionName(this)
                    viewModel.registerUser(registerRequest)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    fun openRoleDialog(v: View){
        MaterialDialog(this).show {
            title(R.string.role)
            listItemsSingleChoice(R.array.rest_role_array, initialSelection = roleIndex) { _, index, text ->
                roleIndex = index
                this@RegistrationActivity.restaurant_role_text.setText(text)
            }
            positiveButton(R.string.choose)

        }
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showSnackBar(progressbar,it) }
            it?.let { println("error>>>>> ${Util.getStringFromBean(it)}") }
        })
        viewModel.registerResponse.observe(this, Observer<RegisterResponse> {
            it?.let {
                println("response>>>>> ${Util.getStringFromBean(it)}")
                if(it.status == Constant.STATUS.FAIL){
                    showSnackBar(progressbar,it.result!!)
                    //callHomePage()
                }else{
                    it.result?.let { it1 -> openSuccesDialog(it1) }
                }

            }

        })

    }

    private fun openSuccesDialog(title:String){
        MaterialDialog(this).show {
            title(R.string.app_name_alert)
            message(null,title)
            positiveButton(R.string.ok)
           positiveButton {
               this@RegistrationActivity.finish()
           }
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
