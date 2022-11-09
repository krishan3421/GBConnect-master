package com.gb.restaurant.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.adduser.AddUserRequest
import com.gb.restaurant.model.adduser.AddUserResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.users.User
import com.gb.restaurant.model.users.UserListReponse
import com.gb.restaurant.model.users.UsersRequest
import com.gb.restaurant.model.users.rmuser.RmUserRequest
import com.gb.restaurant.model.users.rmuser.RmUserResponse
import com.gb.restaurant.ui.adapter.UsersAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.RsLoginViewModel
import kotlinx.android.synthetic.main.activity_user_list.*
import kotlinx.android.synthetic.main.content_user_list.*
import kotlinx.android.synthetic.main.custom_appbar.*

class UserListActivity : BaseActivity() {
    private lateinit var viewModel: RsLoginViewModel
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var usersAdapter:UsersAdapter
    companion object{
        private val TAG = UserListActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        setContentView(R.layout.activity_user_list)
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
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initView(){
        try{
            back_layout.setOnClickListener {
                onBackPressed()
            }
            viewModel = createViewModel()
            attachObserver()
            usersAdapter = UsersAdapter(this,viewModel)
            users_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@UserListActivity)
                adapter = usersAdapter
            }

            callUserListService()
            user_swipe_refresh.setOnRefreshListener {
                callUserListService()
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callUserListService(){
        try {
            var usersRequest=UsersRequest()
            usersRequest.deviceversion =Util.getVersionName(this)
            usersRequest.restaurant_id = rsLoginResponse?.data?.restaurantId?:""
            if(!Validation.isOnline(this)){
                user_swipe_refresh.isRefreshing=false
                Util.alert(getString(R.string.internet_connected),this)
                return
            }
            viewModel.getGbUsers(usersRequest)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    override fun onResume() {
        super.onResume()
        usersAdapter.setOnItemClickListener(object :UsersAdapter.UserClickListener{
            override fun onEditClick(user: User, position: Int, v: View) {
                var  intent = Intent(this@UserListActivity, CreateUserActivity::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra(CreateUserActivity.USER,user)
                intent.putExtra(CreateUserActivity.TYPE,1)//EDIT USER-1
                startActivity(intent)
            }

            override fun onRemoveClick(user: User, position: Int, v: View) {
                removeAlert(user)
            }

        })
    }
    private fun removeAlert(user: User) {
        MaterialDialog(this).show {
            title(R.string.app_name_alert)
            message(R.string.remove_user_msg)
            positiveButton {
                callRmUserService(user)
            }
            negativeButton(R.string.cancel)
            positiveButton(R.string.ok)
        }
    }

    private fun callRmUserService(user:User){
        try {
            var rmUserRequest=RmUserRequest()
            rmUserRequest.deviceversion =Util.getVersionName(this)
            rmUserRequest.restaurant_id =user.rid!!
            rmUserRequest.userid = user.gbId!!
            if(!Validation.isOnline(this)){
                Util.alert(getString(R.string.internet_connected),this)
                return
            }
            viewModel.rmGbUsers(rmUserRequest)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    fun backClick(v: View){
        onBackPressed()
    }


    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showSnackBar(progress_bar,it) }
            user_swipe_refresh.isRefreshing=false
        })
        viewModel.getGbUserResponse.observe(this, Observer<UserListReponse> {
            it?.let {
                println("response>>>>> ${Util.getStringFromBean(it)}")
                user_swipe_refresh.isRefreshing=false
                if(it.status == Constant.STATUS.FAIL){
                    showSnackBar(progress_bar,it.result?:"")
                }else{
                   usersAdapter.notifyDataSetChanged()
                }

            }

        })
        viewModel.rmGbUserResponse.observe(this, Observer<RmUserResponse> {
            it?.let {
                //println("response>>>>> ${Util.getStringFromBean(it)}")
                if(it.status == Constant.STATUS.FAIL){
                    Util.alert(it.result?:"",this@UserListActivity)
                }else{
                   callUserListService()
                }

            }

        })

    }

    private fun createViewModel(): RsLoginViewModel =
        ViewModelProviders.of(this).get(RsLoginViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

}

