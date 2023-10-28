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
import com.gb.restaurant.databinding.ActivityRegistrationBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.register.RegisterRequest
import com.gb.restaurant.model.register.RegisterResponse
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.RsLoginViewModel

class RegistrationActivity : BaseActivity() {
    private lateinit var viewModel: RsLoginViewModel
    private lateinit var binding: ActivityRegistrationBinding
    companion object{
        private val TAG:String = RegistrationActivity::class.java.simpleName
    }
    private  var roleIndex = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_registration)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView(){
        try{
            binding.apply {
                setSupportActionBar(toolbar)
                toolbar.navigationIcon = ContextCompat.getDrawable(this@RegistrationActivity,R.drawable.ic_back)
                toolbar.setNavigationOnClickListener { onBackPressed() }
                contentRegistration.restaurantRoleText.setText("Owner")
                viewModel = createViewModel()
                attachObserver()
            }

        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun submitRegister(v:View){
        try{
            binding.contentRegistration.apply {
                //callHomePage()
                if(!Validation.isOnline(this@RegistrationActivity)){
                    showSnackBar(binding.progressbar,getString(R.string.internet_connected))
                    return
                }else if(nameText.text.toString().isNullOrEmpty()){
                    showSnackBar(binding.progressbar,getString(R.string.name_empty))
                    return
                }else if(!Validation.validateEmail(emailText.text.toString())){
                    showSnackBar(binding.progressbar,getString(R.string.email_empty))
                    return
                }
                else if(!Validation.isValidMobile(molileText.text.toString())){
                    showSnackBar(binding.progressbar,getString(R.string.mobile_empty))
                    return
                } else if(restaurantNameText.text.toString().isNullOrEmpty()){
                    showSnackBar(binding.progressbar,getString(R.string.resturant_name_empty))
                    return
                }else if(restaurantZipText.text.toString().isNullOrEmpty() || restaurantZipText.text.toString().length <5){
                    showSnackBar(binding.progressbar,getString(R.string.zip_validation))
                    return
                }
                else {
                    var registerRequest =RegisterRequest()
                    registerRequest.name =nameText.text.toString()
                    registerRequest.email =emailText.text.toString()
                    registerRequest.phone=molileText.text.toString()
                    registerRequest.rest_name =restaurantNameText.text.toString()
                    registerRequest.rest_zip =restaurantZipText.text.toString()
                    registerRequest.rest_role =restaurantRoleText.text.toString()
                    registerRequest.deviceversion = Util.getVersionName(this@RegistrationActivity)
                    viewModel.registerUser(registerRequest)
                }
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
                binding.contentRegistration.restaurantRoleText.setText(text)
            }
            positiveButton(R.string.choose)

        }
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showSnackBar(binding.progressbar,it) }
            it?.let { println("error>>>>> ${Util.getStringFromBean(it)}") }
        })
        viewModel.registerResponse.observe(this, Observer<RegisterResponse> {
            it?.let {
                println("response>>>>> ${Util.getStringFromBean(it)}")
                if(it.status == Constant.STATUS.FAIL){
                    showSnackBar(binding.progressbar,it.result!!)
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
        if (show) binding.progressbar.visibility = View.VISIBLE else binding.progressbar.visibility = View.GONE
    }


}
