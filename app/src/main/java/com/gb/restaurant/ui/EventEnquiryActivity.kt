package com.gb.restaurant.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.reservation.ReservationRequest
import com.gb.restaurant.model.reservation.ReservationResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.status.EnquiryStatusRequest
import com.gb.restaurant.model.status.StatusResponse
import com.gb.restaurant.push.MyFirebaseMessagingService
import com.gb.restaurant.push.PushMessage
import com.gb.restaurant.ui.adapter.EnquiryAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReservationViewModel
import com.google.android.material.tabs.TabLayout

import kotlinx.android.synthetic.main.activity_event_enquiry.*
import kotlinx.android.synthetic.main.content_event_enquiry.*
import java.util.*
import kotlin.collections.ArrayList

class EventEnquiryActivity : BaseActivity(), TabLayout.OnTabSelectedListener {

    var strtDateCalendar : Calendar?= null
    var endDateCalendar : Calendar?= null
    private lateinit var enquiryAdapter: EnquiryAdapter
    private var list:MutableList<String> = ArrayList()
    private lateinit var viewModel: ReservationViewModel
    var rsLoginResponse: RsLoginResponse? = null
    companion object{
        private val TAG:String = EventEnquiryActivity::class.java.simpleName
        var isEnqueryVisible:Boolean =false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_enquiry)
        setSupportActionBar(toolbar)
        initData()
        initView()
    }



    private fun initData(){
        try {
            viewModel = createViewModel()
            rsLoginResponse = MyApp.instance.rsLoginResponse
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initView(){
        try{
            toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            enquiry_tab_layout.addTab(enquiry_tab_layout.newTab().setText("New"))
            enquiry_tab_layout.addTab(enquiry_tab_layout.newTab().setText("All"))
            enquiry_tab_layout.addTab(enquiry_tab_layout.newTab().setText("Shortlisted"))
            enquiry_tab_layout.addOnTabSelectedListener(this)

            for (i in 0 until enquiry_tab_layout.tabCount) {
                val tab = (enquiry_tab_layout.getChildAt(0) as ViewGroup).getChildAt(i)
                val p = tab.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(0, 0, 3, 0)
                tab.requestLayout()
            }
            enquiryAdapter = EnquiryAdapter(this,viewModel)
            enquiry_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@EventEnquiryActivity)
                adapter = enquiryAdapter
            }

            strtDateCalendar = Calendar.getInstance()
            endDateCalendar = Calendar.getInstance()
            start_date_button.text  = Util.getSelectedDateWithTime(strtDateCalendar!!)
            end_date_button.text  = Util.getSelectedDateWithTime(endDateCalendar!!)
            attachObserver()
            callService()

            enquiry_swipe_refresh.setOnRefreshListener {
                //enquiry_swipe_refresh.isRefreshing = false
                callService()
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }



    private fun callService(){
        try{
            if(Validation.isOnline(this)){
                var reservationRequest = ReservationRequest()
                reservationRequest.deviceversion = Util.getVersionName(this)
                reservationRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                reservationRequest.service_type = Constant.SERVICE_TYPE.GET_ENQUIRY
                if(strtDateCalendar !=null && endDateCalendar !=null) {
                    reservationRequest.datefrom =
                        this@EventEnquiryActivity.start_date_button.text.toString()
                    reservationRequest.dateto =
                        this@EventEnquiryActivity.end_date_button.text.toString()
                    println("data>>>>>>>>>> ${Util.getStringFromBean(reservationRequest)}")
                    viewModel.getReservationResponse(reservationRequest)
                }else{
                    showSnackBar(progress_bar,"Please select valid Date.")
                }

            }else{
                enquiry_swipe_refresh.isRefreshing = false
                showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callStatusService(enquiryStatusRequest: EnquiryStatusRequest){
        try{
            if(Validation.isOnline(this)){
                enquiryStatusRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                println("request>>>>>> ${Util.getStringFromBean(enquiryStatusRequest)}")
                viewModel.getEnquiryStatusRes(enquiryStatusRequest)
            }else{
                showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }



    override fun onResume() {
        super.onResume()
        try{
            isEnqueryVisible =true
            enquiryAdapter.setOnItemClickListener(object :EnquiryAdapter.ReservationClickListener{
                override fun onItemClick(position:Int, v: View) {

                }

            } )

            enquiryAdapter.setOnButtonClickListener(object : EnquiryAdapter.StatusClickListener{
                override fun onButtonClick(enquiryStatusRequest :EnquiryStatusRequest) {
                    callStatusService(enquiryStatusRequest)
                }
            } )
            LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver, IntentFilter(
                MyFirebaseMessagingService.PUSHBROADCAST_ENQUERY) )
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onPause() {
        super.onPause()
        isEnqueryVisible =false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver)
    }

    fun startDateButton(view: View){
        try{
            println("dataclick>>>>>>>>>>>>>")
            var calendar = Calendar.getInstance()
            MaterialDialog(this).show {
                datePicker(null,null,calendar) { _, date ->
                    date.set(date.get(Calendar.YEAR),date.get(Calendar.MONTH),date.get(Calendar.DATE),0,0,0)
                    strtDateCalendar = date
                    var dateText = Util.getSelectedDateWithTime(date)
                    //Util.getSelectedDate(date)?.let { fragmentBaseActivity.showToast(it) }
                    this@EventEnquiryActivity.start_date_button.setText(dateText)

                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    fun endDateButton(view: View){
        try{

            println("dataclick>>>>>>>>>>>>>")
            var calendar = Calendar.getInstance()
            println("time>>> ${calendar.time}")
            MaterialDialog(this).show {
                datePicker(null,null,calendar) { _, date ->
                    if(strtDateCalendar !=null){
                        if(strtDateCalendar!!.before(date) || strtDateCalendar!! == date){
                            date.set(date.get(Calendar.YEAR),date.get(Calendar.MONTH),date.get(Calendar.DATE),23,59,59)
                            endDateCalendar = date
                            var dateText = Util.getSelectedDateWithTime(date)
                            //Util.getSelectedDate(date)?.let { fragmentBaseActivity.showToast(it) }
                            this@EventEnquiryActivity.end_date_button.setText(dateText)
                        }else{
                            this@EventEnquiryActivity.showSnackBar(this@EventEnquiryActivity.toolbar,"start-Date should be equal or less then end-Date")
                        }
                    }else{
                        this@EventEnquiryActivity.showSnackBar(this@EventEnquiryActivity.toolbar,"Please select start-Date")
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun getEvents(view: View){
        try{
            callService()
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
            enquiry_swipe_refresh.isRefreshing = false
            it?.let { showSnackBar(progress_bar,it) }
        })
        viewModel.response.observe(this, Observer<ReservationResponse> {
            enquiry_swipe_refresh.isRefreshing = false
            it?.let {
                enquiryAdapter.notifyDataSetChanged()
                if(enquiryAdapter.itemCount >0){
                    enquiry_recycler.visibility = View.VISIBLE
                    no_order_text.visibility = View.GONE

                }else{
                    enquiry_recycler.visibility = View.GONE
                    no_order_text.visibility = View.VISIBLE
                }
            }
        })

        viewModel.enquiryStatusResponse.observe(this, Observer<StatusResponse> {
            it?.let {
                if(it.status ===Constant.STATUS.SUCCESS){
                    showToast(it.result!!)
                }else{
                    showToast(it.result!!)
                }
            }
        })


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
                if(it.type.equals("Inquiry",true)){
                    showToast("broadcast_Inquiry")
                    callService()
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
}
