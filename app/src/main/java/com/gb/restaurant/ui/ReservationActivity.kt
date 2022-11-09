package com.gb.restaurant.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.push.MyFirebaseMessagingService
import com.gb.restaurant.push.PushMessage
import com.gb.restaurant.ui.fragments.*
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReservationViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_reservation.*
import kotlinx.android.synthetic.main.content_reservation.*

class ReservationActivity : BaseActivity(),TabLayout.OnTabSelectedListener {


    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var samplePagerAdapter: SamplePagerAdapter
    companion object{
        private val TAG:String = ReservationActivity::class.java.simpleName
        var isReservationVisible:Boolean =false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)
        setSupportActionBar(toolbar)
        initData()
        initView()
    }

    private fun initData(){
        try {
            rsLoginResponse = MyApp.instance.rsLoginResponse
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initView(){
        try{
            toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
            toolbar.title = ""//getString(R.string.back)
            title_reser.setOnClickListener { onBackPressed() }
            toolbar.setNavigationOnClickListener { onBackPressed() }
            tab_layout.addTab(tab_layout.newTab().setText("Today"))
            tab_layout.addTab(tab_layout.newTab().setText("Pending"))
            tab_layout.addTab(tab_layout.newTab().setText("Search"))
            tab_layout.addOnTabSelectedListener(this)
            samplePagerAdapter = SamplePagerAdapter(supportFragmentManager)
            reservation_view_pager.adapter = samplePagerAdapter
            tab_layout.setupWithViewPager(reservation_view_pager)
            for (i in 0 until tab_layout.tabCount) {
                val tab = (tab_layout.getChildAt(0) as ViewGroup).getChildAt(i)
                val p = tab.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(0, 0, 3, 0)
                tab.requestLayout()
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }




    override fun onResume() {
        super.onResume()
        try{
            isReservationVisible =true
            LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver, IntentFilter(MyFirebaseMessagingService.PUSHBROADCAST_RESERVATION) )
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onPause() {
        super.onPause()
        isReservationVisible =false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver)
    }

    inner  class  SamplePagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm){
        override fun getItem(position: Int): Fragment {
            return when {
                position === 0 -> {
                    TodayFragment()
                }
                position === 1 -> {
                    PendingFragment()
                }
                else -> {
                    SearchFragment()
                }
            }
        }

        override fun getCount(): Int {
            return 3
        }
        override fun getPageTitle(position: Int): CharSequence? {
            var title: String? = null
            when (position) {
                0 -> {
                    title = "Today"
                }
                1 -> {
                    title = "Pending"//Active
                }
                2 -> {
                    title = "Search"
                }
            }
            return title
        }

        override fun getItemPosition(`object`: Any): Int {
            // POSITION_NONE makes it possible to reload the PagerAdapter
            return PagerAdapter.POSITION_NONE
        }
    }

    private fun createViewModel(): ReservationViewModel =
        ViewModelProviders.of(this).get(ReservationViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }


    override fun onTabReselected(p0: TabLayout.Tab?) {
    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {
    }

    override fun onTabSelected(p0: TabLayout.Tab?) {

        when(p0!!.text){
            "Today"->{
            }
            "Pending"->{

            }
            "Search"->{
            }

        }

    }

    var pushBroadcastReceiver=object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            //showToast("broadcast>>>>>")
            val pushMessage = intent?.getParcelableExtra<PushMessage>(MyFirebaseMessagingService.PUSH_KEY)
            println("datapush>>>>> ${pushMessage?.let { Util.getStringFromBean(it) }}")
            pushMessage?.let {handlePushMessage(it)  }

        }

    }

    private fun handlePushMessage(pushMessage: PushMessage){
        try{
            pushMessage?.let {
                if(it.type.equals("Reservation",true)){
                     //showToast("broadcast_reservation")
                    TodayFragment.getInstance()?.initCallService()
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onBackPressed() {
        var intent = Intent()
        setResult(Activity.RESULT_OK,intent)
        finish()
        //super.onBackPressed()
    }
}
