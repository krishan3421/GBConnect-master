package com.gb.restaurant.ui.fragments

import android.Manifest
import android.Manifest.permission.BLUETOOTH_SCAN
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.*
import com.gb.restaurant.utils.Util
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.home_fragment.*

class HomeFragment : BaseFragment() ,View.OnClickListener,InstallStateUpdatedListener{
    var rsLoginResponse: RsLoginResponse? = null
    lateinit var  appUpdateManager: AppUpdateManager
    var isFromOrder:Boolean=false
    companion object {
        fun newInstance() = HomeFragment()
        private val TAG = HomeFragment::class.java.simpleName
        private const val RC_APP_UPDATE = 11
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rsLoginResponse = MyApp.instance.rsLoginResponse
        checkUpdate()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orders_layout.setOnClickListener(this)
        report_layout.setOnClickListener(this)
        setting_layout.setOnClickListener(this)
        support_layout.setOnClickListener(this)
        market_coupon_layout.setOnClickListener(this)
        rsLoginResponse?.data?.let {
            if(!it.version.isNullOrEmpty()){
                println("version>>>>>> ${it.version}  ${Util.getVersionName(fragmentBaseActivity).toFloat()}")
                if(it.version!!.toFloat() > Util.getVersionName(fragmentBaseActivity).toFloat()){
                    update_layout.visibility=View.VISIBLE
                }
            }
        }
       // date_text.text = "${Util.getCurrentDateHome()}"
        var updateString = SpannableString(getString(R.string.new_update))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                updateApp()
               // fragmentBaseActivity.showToast("clickable")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }


        updateString.setSpan(clickableSpan,50, updateString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        updateString.setSpan(ForegroundColorSpan(Color.BLUE), 50, updateString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        update_text.text =updateString
        update_text.movementMethod = LinkMovementMethod.getInstance();
        close_update_icon.setOnClickListener {
            update_layout.visibility=View.GONE
        }

        if(!rsLoginResponse?.data?.gbtype.equals("Admin",true)){
            support_layout.alpha=0.4f
            support_layout.isClickable=false
            setting_layout.alpha=0.4f
            setting_layout.isClickable=false
            report_layout.alpha=0.4f
            report_layout.isClickable=false
            market_coupon_layout.alpha=0.4f
            market_coupon_layout.isClickable=false
        }

    }

    private fun updateApp(){
        try{
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${Util.getPackageName(fragmentBaseActivity)}")
                )

            )
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    override fun onClick(p0: View?) {
        when(p0){
            orders_layout->{
                //openActivity(OrdersActivity::class.java)
                isFromOrder =true
                checkBlueToothPermission()
            }
            report_layout->{
                openActivity(NewReportActivity::class.java)
            }
            setting_layout->{
                isFromOrder=false
                checkBlueToothPermission()
            }
            support_layout->{
                openActivity(SupportActivity::class.java)
               // showCustomViewDialog()
            }
            market_coupon_layout->{
                //fragmentBaseActivity.showToast("this f")
                openActivity(MarketCouponActivity::class.java)
               // fragmentBaseActivity.showToast("Comming soon")
            }
        }
    }

    private fun checkBlueToothPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    fragmentBaseActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    fragmentBaseActivity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                makeRequest()
            } else {
                if (isFromOrder) {
                    openActivity(OrdersActivity::class.java)
                } else {
                    openActivity(SettingActivity::class.java)
                }

            }
        }else{
            if (isFromOrder) {
                openActivity(OrdersActivity::class.java)
            } else {
                openActivity(SettingActivity::class.java)
            }
        }
    }

    private fun makeRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
            2222)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            2222 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    if(isFromOrder){
                        openActivity(OrdersActivity::class.java)
                    }else{
                        openActivity(SettingActivity::class.java)
                    }
                } else {
                    fragmentBaseActivity?.showToast("Bluetooth Permission not granted")

                }
            }
        }

    }

    private fun openActivity(cls:Class<*>?){
        var  intent = Intent(fragmentBaseActivity, cls)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun checkUpdate(){
        try {
            // Creates instance of the manager.
             appUpdateManager = AppUpdateManagerFactory.create(context)

// Returns an intent object that you use to check for an update.
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
          //  appUpdateManager.registerListener(this)
// Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    //&& appUpdateInfo.updatePriority() >= 5//HIGH_PRIORITY_UPDATE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.IMMEDIATE,activity, RC_APP_UPDATE)

                }else{
                    println("Update not available")
                }
            }
            appUpdateInfoTask.addOnFailureListener { e: Exception? ->
                e?.printStackTrace()
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("app>>>>>>> updated")
        if(resultCode==Activity.RESULT_OK){
            if(requestCode==RC_APP_UPDATE){
                println("app>>>>>>> updated")
                fragmentBaseActivity.showToast("App Update Successfully.")
            }else{
                println("app not updated")
                fragmentBaseActivity.showToast("App Update failed.Please try again.")
            }
        }else{
            println("app>>>>>>>not RESULT_OK")
        }
    }


    override fun onStateUpdate(state: InstallState) {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            popupSnackbarForCompleteUpdate()
        }

    }

    /* Displays the snackbar notification and call to action. */
    fun popupSnackbarForCompleteUpdate() {
        MaterialDialog(fragmentBaseActivity).show {
            title(R.string.app_name)
            cancelable(false)
            message(null,"An update has just been downloaded.")
            positiveButton {
                appUpdateManager.completeUpdate()
            }
            positiveButton(R.string.restart)
    }
    }
}
