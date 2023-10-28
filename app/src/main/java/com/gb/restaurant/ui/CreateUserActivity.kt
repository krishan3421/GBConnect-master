package com.gb.restaurant.ui

import android.content.Intent
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
import com.gb.restaurant.databinding.ActivityConfirmTimeDialogBinding
import com.gb.restaurant.databinding.ActivityCreateUserBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.adduser.AddUserRequest
import com.gb.restaurant.model.adduser.AddUserResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.users.User
import com.gb.restaurant.model.users.edituser.EditUserRequest
import com.gb.restaurant.model.users.edituser.EditUserResponse
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.RsLoginViewModel

class CreateUserActivity : BaseActivity() {
    private lateinit var viewModel: RsLoginViewModel
    var rsLoginResponse: RsLoginResponse? = null
    var user:User?=null
    var type:Int=0
    private lateinit var binding: ActivityCreateUserBinding
    companion object{
        private val TAG = CreateUserActivity::class.java.simpleName
         val USER = "USER"
         val TYPE = "TYPE"//CREATE_USER-0,EDIT_USER-1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        //setContentView(R.layout.activity_create_user)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }

    private fun statusBarTransparent(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
      /*  if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }*/
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    private fun initData(){
        try{
            rsLoginResponse = MyApp.instance.rsLoginResponse
            intent?.let {
                user =it.getParcelableExtra(USER)
                type=it.getIntExtra(TYPE,0)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initView(){
        try{
            binding.apply {
                customAppbar.backLayout.setOnClickListener {
                    onBackPressed()
                }
                viewModel = createViewModel()
                attachObserver()
                if(type==1){
                    contentCreateUser.createUserHeader.text = "Edit User"
                    contentCreateUser.createUserButton.text="Save Changes"
                    contentCreateUser.userIdText.isFocusable=false
                    contentCreateUser.userNameText.setText("${user?.name}")
                    contentCreateUser.userIdText.setText("${user?.gbId}")
                    contentCreateUser.passwordText.setText("${user?.gbPass}")
                }
            }

        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    fun addUserMethod(view:View){
        try {
            checkValidation()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    fun usersListMethod(view:View){
        try {
            var  intent = Intent(this, UserListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    private fun checkValidation(){
        binding.contentCreateUser.apply {
            try{
                if(userNameText.text.isNullOrEmpty()){
                    Util.alert("Name should not empty.",this@CreateUserActivity)
                    return
                }
                if(userIdText.text.isNullOrEmpty()){
                    Util.alert("User-Id should not empty.",this@CreateUserActivity)
                    return
                }
                if(passwordText.text.isNullOrEmpty()){
                    Util.alert("Password should not empty.",this@CreateUserActivity)
                    return
                }
                if(type==0) {
                    var addUserRequest = AddUserRequest()
                    addUserRequest.restaurant_id = rsLoginResponse?.data?.restaurantId ?: ""
                    addUserRequest.name = userNameText.text.toString()
                    addUserRequest.userid = userIdText.text.toString()
                    addUserRequest.userpass = passwordText.text.toString()
                    addUserRequest.deviceversion = Util.getVersionName(this@CreateUserActivity)
                    callAddUserService(addUserRequest)
                }else{
                    var editUserRequest = EditUserRequest()
                    editUserRequest.restaurant_id = user?.rid!!
                    editUserRequest.name = userNameText.text.toString()
                    editUserRequest.userid = user?.gbId!!
                    editUserRequest.userpass = passwordText.text.toString()
                    editUserRequest.deviceversion = Util.getVersionName(this@CreateUserActivity)
                    callEditUserService(editUserRequest)
                }
            }catch (e:Exception){
                e.printStackTrace()
                Log.e(TAG, e.message!!)
            }
        }

    }
    private fun callAddUserService(addUserRequest: AddUserRequest){
        try {
            if(!Validation.isOnline(this)){
                Util.alert(getString(R.string.internet_connected),this)
                return
            }
            viewModel.addGbUser(addUserRequest)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    private fun callEditUserService(editUserRequest: EditUserRequest){
        try {
            if(!Validation.isOnline(this)){
                Util.alert(getString(R.string.internet_connected),this)
                return
            }
            viewModel.editGbUser(editUserRequest)
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
           // it?.let { println("error>>>>> ${Util.getStringFromBean(it)}") }
        })
        viewModel.addUserResponse.observe(this, Observer<AddUserResponse> {
            it?.let {
                println("response>>>>> ${Util.getStringFromBean(it)}")
                if(it.status == Constant.STATUS.FAIL){
                    showSnackBar(binding.progressBar,it.result?:"")
                }else{
                    binding.contentCreateUser.userNameText.setText("")
                    binding.contentCreateUser.userIdText.setText("")
                    binding.contentCreateUser.passwordText.setText("")
                    Util.alert(it.result?:"",this)
                }

            }

        })
        viewModel.editGbUserResponse.observe(this, Observer<EditUserResponse> {
            it?.let {
                if(it.status == Constant.STATUS.FAIL){
                    showSnackBar(binding.progressBar,it.result?:"")
                }else{
                    //Util.alert(it.result?:"",this)
                    editSuccessAlert(it.result?:"")
                }

            }

        })

    }

    fun editSuccessAlert(message:String){
        try {
            MaterialDialog(this).show {
                title(R.string.app_name_alert)
                message(null,message)
                positiveButton {
                  this@CreateUserActivity.onBackPressed()
                }
                positiveButton(R.string.ok)

            }
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
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

}
