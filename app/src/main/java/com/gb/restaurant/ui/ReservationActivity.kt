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
import com.gb.restaurant.databinding.ActivityReportsDetailBinding
import com.gb.restaurant.databinding.ActivityReservationBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.push.MyFirebaseMessagingService
import com.gb.restaurant.push.PushMessage
import com.gb.restaurant.ui.fragments.*
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReservationViewModel
import com.google.android.material.tabs.TabLayout

class ReservationActivity : BaseActivity(),TabLayout.OnTabSelectedListener {


    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var samplePagerAdapter: SamplePagerAdapter
    private lateinit var binding: ActivityReservationBinding
    companion object{
        private val TAG:String = ReservationActivity::class.java.simpleName
        var isReservationVisible:Boolean =false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_reservation)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
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
            binding.toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
            binding.toolbar.title = ""//getString(R.string.back)
            binding.titleReser.setOnClickListener { onBackPressed() }
            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
            supportActionBar?.setDisplayShowTitleEnabled(false)
            binding.contentReservation.tabLayout.addTab(binding.contentReservation.tabLayout.newTab().setText("Today"))
            binding.contentReservation.tabLayout.addTab(binding.contentReservation.tabLayout.newTab().setText("Pending"))
            binding.contentReservation.tabLayout.addTab(binding.contentReservation.tabLayout.newTab().setText("Search"))
            binding.contentReservation.tabLayout.addOnTabSelectedListener(this)
            samplePagerAdapter = SamplePagerAdapter(supportFragmentManager)
            binding.contentReservation.reservationViewPager.adapter = samplePagerAdapter
            binding.contentReservation.tabLayout.setupWithViewPager(binding.contentReservation.reservationViewPager)
            for (i in 0 until binding.contentReservation.tabLayout.tabCount) {
                val tab = (binding.contentReservation.tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
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
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
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
            //println("datapush>>>>> ${pushMessage?.let { Util.getStringFromBean(it) }}")
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
