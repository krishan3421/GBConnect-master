package com.gb.restaurant.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.reservation.*
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.status.ReserStatusRequest
import com.gb.restaurant.model.status.StatusResponse
import com.gb.restaurant.ui.adapter.ReservationAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReservationViewModel
import kotlinx.android.synthetic.main.fragment_today.*
import kotlinx.android.synthetic.main.reservation_popup_layout.*
import kotlinx.android.synthetic.main.reservation_popup_layout.close_dialog
import kotlinx.android.synthetic.main.reservation_popup_layout.confirm_button
import kotlinx.android.synthetic.main.reservation_popup_layout.title_dialog_text
import kotlinx.android.synthetic.main.stop_reservation_layout.*
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TodayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TodayFragment : BaseFragment(),View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var reservationAdapter: ReservationAdapter
    private var list:MutableList<String> = ArrayList()
    private lateinit var viewModel: ReservationViewModel
    var rsLoginResponse: RsLoginResponse? = null
    private  var reservationRequest =ReservationRequest()
    private var stopDialog:MaterialDialog?=null
    private var dateFrom:String = ""
    private var dateTo:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       instance =this;
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        rsLoginResponse = MyApp.instance.rsLoginResponse
        viewModel = createViewModel()
        attachObserver()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_today, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            stop_reservation_button.setOnClickListener(this)

            reservationAdapter = ReservationAdapter(fragmentBaseActivity,viewModel)
            reservation_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(fragmentBaseActivity)
                adapter = reservationAdapter
            }
            reservation_swipe_refresh.setOnRefreshListener {
                callService(reservationRequest)
                //reservation_swipe_refresh.isRefreshing = false
            }
            initCallService()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    fun initCallService(){
        try{
            var todayDate = Util.getSelectedDateWithTime(Calendar.getInstance())
            reservationRequest.datefrom =todayDate!!
            reservationRequest.dateto =todayDate!!
            reservationRequest.search_type = "Today"
            callService(reservationRequest)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callService(reservationRequest :ReservationRequest){
        try{
            if(Validation.isOnline(fragmentBaseActivity)){
                reservationRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                reservationRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                viewModel.getReservationResponse(reservationRequest)
            }else{
                reservation_swipe_refresh.isRefreshing = false
                fragmentBaseActivity.showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callStatusService(reserStatusRequest: ReserStatusRequest){
        try{
            if(Validation.isOnline(fragmentBaseActivity)){
                reserStatusRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                reserStatusRequest.service_type = Constant.SERVICE_TYPE.GET_RESERVATION_STATUS
                reserStatusRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                println("request>>>>>> ${Util.getStringFromBean(reserStatusRequest)}")
                viewModel.getReservationStatusRes(reserStatusRequest)
            }else{
                fragmentBaseActivity.showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    override fun onResume() {
        super.onResume()
        try{
            reservationAdapter.setOnItemClickListener(object :ReservationAdapter.ReservationClickListener{
                override fun onItemClick(data: Data, position:Int, v: View) {
                    showReservationDialog(data)
                }

            } )

            reservationAdapter.setOnButtonClickListener(object :ReservationAdapter.StatusClickListener{
                override fun onButtonClick(reserStatusRequest: ReserStatusRequest) {
                   // callStatusService(reserStatusRequest)
                }
            } )
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
            reservation_swipe_refresh.isRefreshing = false
            it?.let { fragmentBaseActivity.showSnackBar(progress_bar,it) }
        })
        viewModel.response.observe(this, Observer<ReservationResponse> {
            reservationAdapter.notifyDataSetChanged()
            reservation_swipe_refresh.isRefreshing = false
            if(reservationAdapter.itemCount >0){
                reservation_recycler.visibility = View.VISIBLE
                no_order_text.visibility = View.GONE

            }else{
                reservation_recycler.visibility = View.GONE
                no_order_text.visibility = View.VISIBLE
            }
        })
        viewModel.resStatusResponse.observe(this, Observer<StatusResponse> {
            it?.let {
                println("status response>>>>>>> ${Util.getStringFromBean(it)}")
                if(it.status ==Constant.STATUS.SUCCESS){
                    fragmentBaseActivity.showToast(it.result?:"")
                    initCallService()
                }else{
                    fragmentBaseActivity.showToast(it.result?:"")
                }
            }
        })

        viewModel.stopReserResponse.observe(this, Observer<StopReservationResponse> {
            it?.let {
                stopDialog?.stop_progress_bar?.visibility =View.GONE
                if(it.status == Constant.STATUS.SUCCESS){
                    stopDialog?.dismiss()
                    fragmentBaseActivity.showToast(it.result?:"")

                }else{
                    fragmentBaseActivity.showToast(it.result!!)
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

    fun startDateTimePopUp(startDate:Boolean=false){
        try{
            MaterialDialog(fragmentBaseActivity).show {
                title(text = "Select Date and Time")
                dateTimePicker(requireFutureDateTime = true,show24HoursView=true) { _, dateTime ->
                    dateTime.set(dateTime.get(Calendar.YEAR),dateTime.get(Calendar.MONTH),dateTime.get(Calendar.DATE),
                        dateTime.get(Calendar.HOUR_OF_DAY),dateTime.get(Calendar.MINUTE),dateTime.get(Calendar.SECOND))
                    if(startDate){
                        stopDialog?.from_date_text?.text = Util.getMMM_DD_YYYY(dateTime.time)
                    }else{
                        stopDialog?.select_date_text?.visibility = View.VISIBLE
                        stopDialog?.select_date_text?.text = Util.getMMM_DD_YYYY(dateTime.time)
                    }
                    dateFrom = Util.getyyyy_mm_dd_hh_mm_ss(dateTime.time)?:""
                    println("date>>> $dateFrom")
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    fun toDateTimePopUp(){
        try{
            MaterialDialog(fragmentBaseActivity).show {
                title(text = "Select Date and Time")
                dateTimePicker(requireFutureDateTime = true,show24HoursView=true) { _, dateTime ->
                    dateTime.set(dateTime.get(Calendar.YEAR),dateTime.get(Calendar.MONTH),dateTime.get(Calendar.DATE),
                        dateTime.get(Calendar.HOUR_OF_DAY),dateTime.get(Calendar.MINUTE),dateTime.get(Calendar.SECOND))
                    stopDialog?.to_date_text?.text = Util.getMMM_DD_YYYY(dateTime.time)
                    dateTo = Util.getyyyy_mm_dd_hh_mm_ss(dateTime.time)?:""
                    println("date>>> ${Util.getyyyy_mm_dd_hh_mm_ss(dateTime.time)}")
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    companion object {
        private var instance:TodayFragment?=null

        @Synchronized
        fun getInstance(): TodayFragment? {
            return instance
        }
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TodayFragment.
         */
        val TAG:String = TodayFragment::class.java.simpleName
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TodayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(view: View?) {
        try {
            when(view){
                stop_reservation_button->{
                    showStopReservationDialog()
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    private fun showStopReservationDialog(){
        try{
            val dialog = MaterialDialog(fragmentBaseActivity).show {
                this.cancelOnTouchOutside(false)
                cornerRadius(null,R.dimen.dimen_30)
                customView(R.layout.stop_reservation_layout, scrollable = false, noVerticalPadding = true, horizontalPadding = false)

                title_stop_text.text = "Stop Reservation"
                stopDialog = this
                from_date_text.text = Util.getMMM_DD_YYYY(Date())
                to_date_text.text = Util.getMMM_DD_YYYY(Date())
                from_date_text.setOnClickListener {
                    startDateTimePopUp(true)
                }
                to_date_text.setOnClickListener {
                    toDateTimePopUp()
                }
                var radio: RadioButton =stop_today_radio
                radio_group.setOnCheckedChangeListener { radioGroup, checkedId  ->
                     radio = radioGroup.findViewById(checkedId)
                   when(radio){
                       stop_today_radio->{
                           select_date_text?.visibility = View.INVISIBLE
                           from_to_date_layout.visibility = View.GONE
                          // date_layout.visibility = View.GONE
                       }
                       select_date_radio->{
                           from_to_date_layout.visibility = View.GONE
                           startDateTimePopUp(false)
                           //date_layout.visibility = View.VISIBLE
                           //end_date_layout.visibility = View.GONE
                       }
                       stop_betwwen_radio->{
                           from_to_date_layout.visibility = View.VISIBLE
                           select_date_text?.visibility = View.INVISIBLE
                           //date_layout.visibility = View.VISIBLE
                          // end_date_layout.visibility = View.VISIBLE
                       }
                   }
                }
                close_dialog_stop.setOnClickListener {
                    this.dismiss()
                }
                confirm_button_stop.setOnClickListener {
                    var reservationStopRequest= ReservationStopRequest()
                    when(radio){
                        stop_today_radio->{
                            reservationStopRequest.stop = Constant.RESERVATION_STOP.TODAY
                        }
                        select_date_radio->{
                            reservationStopRequest.stop = Constant.RESERVATION_STOP.DAY
                            reservationStopRequest.datefrom =dateFrom
                        }
                        stop_betwwen_radio->{
                            reservationStopRequest.stop = Constant.RESERVATION_STOP.BETWEEN
                            reservationStopRequest.datefrom =dateFrom
                            reservationStopRequest.dateto =dateTo
                        }
                    }
                    stop_progress_bar.visibility =View.VISIBLE
                    callStopReserVationService(reservationStopRequest)
                }

            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    private fun callStopReserVationService(reservationStopRequest: ReservationStopRequest){
        try{
            if(Validation.isOnline(fragmentBaseActivity)){
                reservationStopRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                reservationStopRequest.service_type = Constant.SERVICE_TYPE.GET_RESERVATION_STOP
                reservationStopRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                println("request>>>>>> ${Util.getStringFromBean(reservationStopRequest)}")
                viewModel.stopReservation(reservationStopRequest)
            }else{
                fragmentBaseActivity.showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun showReservationDialog(data:Data) {
       // println("data>>>> ${Util.getStringFromBean(data)}")
        val dialog = MaterialDialog(fragmentBaseActivity).show {
            this.cancelOnTouchOutside(false)
            cornerRadius(null,R.dimen.dimen_30)
            customView(R.layout.reservation_popup_layout, scrollable = false, noVerticalPadding = true, horizontalPadding = false)
            title_dialog_text.text = "${data.name?:""}"
             people_text.text ="${data.peoples}"
            date_text.text ="Date: ${data.date2}"
            booking_date_text.text ="Booking Date: ${data.bookingtime}"
            if(data.location.isNullOrEmpty()){
                location_text.visibility = View.GONE
                location_type.visibility = View.GONE
            }else{
                location_text.text ="${data.location}"
            }

             des_text.text = "${data.details}"
            if(data.reply.isNullOrEmpty()){
                if(data.status.equals(Constant.ORDER_STATUS.CLOSED,true)){
                    reply_edittext.visibility = View.GONE
                    confirm_button.visibility=View.GONE
                    cancel_button.text = "${data.status}"
                    cancel_button.alpha = 0.5f
                    cancel_button.isClickable=false
                    cancel_button.isEnabled=false
                }else if(data.status.equals(Constant.ORDER_STATUS.CONFIRMED,true)){
                    reply_edittext.visibility = View.GONE
                    confirm_button.text = "${data.status}"
                    confirm_button.alpha = 0.5f
                    confirm_button.isClickable=false
                    confirm_button.isEnabled=false
                    cancel_button.text = "CLOSE"
                }
                else if(data.status.equals(Constant.ORDER_STATUS.CANCEL,true)){
                    reply_edittext.visibility = View.GONE
                    confirm_button.visibility=View.GONE
                    cancel_button.text = "CANCELED"
                    cancel_button.alpha = 0.5f
                    cancel_button.isClickable=false
                    cancel_button.isEnabled=false
                }else{
                    reply_edittext.visibility = View.VISIBLE
                }
                reply_text.visibility = View.GONE
                reply_text_val.visibility = View.GONE
            }else{
                reply_text.text = "${data.reply}"
                reply_text.visibility = View.VISIBLE
                reply_edittext.visibility = View.GONE
                reply_text_val.visibility = View.VISIBLE
                if(data.status.equals(Constant.ORDER_STATUS.CLOSED,true)){
                    confirm_button.visibility=View.GONE
                    cancel_button.text = "${data.status}"
                    cancel_button.alpha = 0.5f
                    cancel_button.isClickable=false
                    cancel_button.isEnabled=false
                }else if(data.status.equals(Constant.ORDER_STATUS.CONFIRMED,true)){
                    confirm_button.text = "${data.status}"
                    confirm_button.alpha = 0.5f
                    confirm_button.isClickable=false
                    confirm_button.isEnabled=false
                    cancel_button.text = "CLOSE"
                }
                else if(data.status.equals(Constant.ORDER_STATUS.CANCEL,true)){
                    confirm_button.visibility=View.GONE
                    cancel_button.text = "CANCELED"
                    cancel_button.alpha = 0.5f
                    cancel_button.isClickable=false
                    cancel_button.isEnabled=false
                }
            }

            close_dialog.setOnClickListener {
                this.dismiss()
            }
            confirm_button.setOnClickListener {
                var reserStatusRequest= ReserStatusRequest()
                reserStatusRequest.reservation_id = data.id!!
                reserStatusRequest.reply = reply_edittext.text.toString()
                reserStatusRequest.status = Constant.ORDER_STATUS.CONFIRMED
                callStatusService(reserStatusRequest)
                this.dismiss()
            }
            cancel_button.setOnClickListener {
                var reserStatusRequest= ReserStatusRequest()
                reserStatusRequest.reservation_id = data.id!!
                reserStatusRequest.reply = reply_edittext.text.toString()
                if(cancel_button.text.toString().equals(Constant.ORDER_STATUS.CANCEL,true)) {
                    reserStatusRequest.status = Constant.ORDER_STATUS.CANCEL
                }else{
                    reserStatusRequest.status = Constant.ORDER_STATUS.CLOSED
                }
                callStatusService(reserStatusRequest)
                this.dismiss()
            }
        }

    }

    private fun stopReservationService(reservationStopRequest: ReservationStopRequest){
        try{
            if(Validation.isOnline(fragmentBaseActivity)){
                reservationStopRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                reservationStopRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                println("request>>>>>> ${Util.getStringFromBean(reservationStopRequest)}")
            }else{
                Util.alertDialog(getString(R.string.internet_connected),fragmentBaseActivity)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
}
