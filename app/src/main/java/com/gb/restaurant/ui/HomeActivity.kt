package com.gb.restaurant.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.ActivityEventEnquiryBinding
import com.gb.restaurant.databinding.ActivityHomeBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.logout.LogoutRequest
import com.gb.restaurant.model.logout.LogoutResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.push.NotificationHelper
import com.gb.restaurant.push.PushMessage
import com.gb.restaurant.ui.fragments.FragmentTags
import com.gb.restaurant.ui.fragments.HomeFragment
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.RsLoginViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.gb.restaurant.session.SessionManager
import com.gb.restaurant.ui.fragments.BlankFragment


class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewModel: RsLoginViewModel
companion object{
    private val TAG = HomeActivity::class.java.simpleName
}
     lateinit var nameRestText:TextView
    lateinit var addressText:TextView
    lateinit var delPackgText:TextView
    lateinit var pickupPackgText:TextView
    lateinit var createUserButton:TextView
   var fromLogout:Boolean=false

    var loginResponse:RsLoginResponse?=null
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        //setContentView(R.layout.activity_home)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginResponse = MyApp.instance.rsLoginResponse
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        try{
            //https://stackoverflow.com/questions/38237559/how-do-you-send-a-firebase-notification-to-all-devices-via-curl
            FirebaseMessaging.getInstance().subscribeToTopic("all");//in postman to--->> /topics/all
            FirebaseMessaging.getInstance().subscribeToTopic("${loginResponse?.data?.restaurantId}");
        }catch (e:Exception){
            e.printStackTrace()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val headerView: View = navView.getHeaderView(0)
        nameRestText = headerView.findViewById(R.id.restaurant_name_text)
        addressText = headerView.findViewById(R.id.address_text)
        delPackgText = headerView.findViewById(R.id.del_pckg_text)
        pickupPackgText = headerView.findViewById(R.id.pickup_pckg_text)
        createUserButton= headerView.findViewById(R.id.create_user_text)
        nameRestText.text = "${loginResponse?.data?.name}"
        addressText.text = "${loginResponse?.data?.address}"
        delPackgText.text = "Delivery Orders - ${loginResponse?.data?.del_pckg}"
        pickupPackgText.text = "Pickup Orders - ${loginResponse?.data?.pick_pckg}"
        if(loginResponse?.data?.gbtype.equals("Admin",true)){
            createUserButton.visibility=View.VISIBLE
        }else{
            createUserButton.visibility=View.GONE
        }
        createUserButton.setOnClickListener {
            var  intent = Intent(this, CreateUserActivity::class.java)
            //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(CreateUserActivity.TYPE,0)//Create USER-0
            startActivity(intent)
        }
        binding.appBarHome.notificationImage.setOnClickListener {
            if(MyApp.instance.isAlarm){
                binding.appBarHome.notificationImage.setImageResource(R.drawable.ic_notification_off)
                MyApp.instance.isAlarm =false

            }else{
                binding.appBarHome.notificationImage.setImageResource(R.drawable.ic_notification_active)
                MyApp.instance.isAlarm =true
            }

        }
        navView.setNavigationItemSelectedListener(this)
        addHomeFragment(HomeFragment.newInstance(),FragmentTags.HOME_FRAGMENT)
        viewModel = createViewModel()
        attachObserver()
        checkBundle(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        try{
            checkBundle(intent)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    private fun checkBundle(intent: Intent?):Boolean{
        var fromNotification:Boolean=false
        try{
            val bundle: Bundle? = intent?.getBundleExtra(NotificationHelper.PUSH_KEY)
            bundle?.let {
                bundle.apply {
                    fromNotification = getBoolean(NotificationHelper.FROMNOTIFICATION,false)
                    val pushMessage: PushMessage? = getParcelable(NotificationHelper.PUSH_KEY)
                    pushMessage?.let { handlePushMessage(it) }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
        return fromNotification
    }
    private fun handlePushMessage(pushMessage: PushMessage){
        try{
            pushMessage?.let {
                if(it.type.equals("Reservation",true)){
                    openActivity(ReservationActivity::class.java)
                }else if(it.type.equals("Inquiry",true)){
                    openActivity(EventEnquiryActivity::class.java)
                }
                else if(it.type.equals("OrderNew",true)){
                    openActivity(OrdersActivity::class.java)
                }
                else if(it.type.equals("OrderHold",true)){
                    openActivity(OrdersActivity::class.java)
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    private fun openActivity(cls:Class<*>?){
        var  intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    private fun statusBarTransparent(){
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
       /* if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }*/
//        if (Build.VERSION.SDK_INT >= 19) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        }
       /* if (Build.VERSION.SDK_INT >= 21) {
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

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            backPopUp(getString(R.string.exit_app))
           // super.onBackPressed()
        }
    }

    private fun backPopUp(msg:String,fromLogout:Boolean=false){
        try{
            MaterialDialog(this).show {
                title(R.string.app_name_alert)
                message(null,msg)
                positiveButton {
                    this@HomeActivity.fromLogout=fromLogout
                    callLogoutService()
                }
                positiveButton(R.string.ok)
                negativeButton(R.string.cancel)

            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callLogoutService(){
        try{
            if(Validation.isOnline(this)){
                var logoutRequest=LogoutRequest()
                logoutRequest.login_id = loginResponse?.data?.loginId?:""
                logoutRequest.deviceversion = Util.getVersionName(this)
                viewModel.logout(logoutRequest)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

   /* override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
          R.id.nav_reset_pass -> {
              var  intent = Intent(this, ResetPassActivity::class.java)
              intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
              startActivity(intent)
            }
            R.id.nav_bank_info -> {
                var  intent = Intent(this, BankInfoActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
             R.id.nav_logout -> {
                 backPopUp(getString(R.string.logout_app),true)
            }
            /* R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }*/
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun addHomeFragment(fragment: Fragment, TAG: String) {
        try {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(binding.contentHome.containerHome.id, fragment, TAG)
            fragmentTransaction.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)

        }

    }

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            // it?.let { showSnackBar(progressbar,getString(R.string.service_error)) }
            // it?.let { println("error>>>>> ${Util.getStringFromBean(it)}") }
           // Util.alert(getString(R.string.service_error),this)
           logout()
        })

        viewModel.logoutResponse.observe(this, Observer<LogoutResponse> {
            logout()
        })

    }

    override fun onDestroy() {
        logout()
        super.onDestroy()
    }
    private fun logout(){
        try{

            MyApp.instance.rsLoginResponse=null
            if(fromLogout){
                var sessionManager= SessionManager(this)
                sessionManager.logout()
                try{
                    //https://stackoverflow.com/questions/38237559/how-do-you-send-a-firebase-notification-to-all-devices-via-curl
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("all");//in postman to--->> /topics/all
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("${loginResponse?.data?.restaurantId}");
                }catch (e:Exception){
                    e.printStackTrace()
                }
                var  intent = Intent(this@HomeActivity, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            finish()
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
