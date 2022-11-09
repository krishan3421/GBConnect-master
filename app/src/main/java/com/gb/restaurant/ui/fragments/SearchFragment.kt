package com.gb.restaurant.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.afollestad.date.dayOfMonth
import com.afollestad.date.month
import com.afollestad.date.year
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

import com.gb.restaurant.R
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.reservation.ReservationResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.ReservationAdapter
import com.gb.restaurant.utils.Util
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.customview.customView
import com.gb.restaurant.Validation
import com.gb.restaurant.model.reservation.Data
import com.gb.restaurant.model.reservation.ReservationRequest
import com.gb.restaurant.model.status.ReserStatusRequest
import com.gb.restaurant.model.status.StatusResponse
import com.gb.restaurant.viewmodel.ReservationViewModel
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.reservation_popup_layout.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PendingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : BaseFragment(),View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var strtDateCalendar : Calendar?= null
    var endDateCalendar :Calendar?= null
    private lateinit var reservationAdapter: ReservationAdapter
    private var list:MutableList<String> = ArrayList()
    private lateinit var viewModel: ReservationViewModel
    var rsLoginResponse: RsLoginResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        viewModel = createViewModel()
        attachObserver()
        rsLoginResponse = MyApp.instance.rsLoginResponse
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            end_date_layout.setOnClickListener(this)
            start_date_layout.setOnClickListener(this)
            start_date_button_search.setOnClickListener(this)
            end_date_button_search.setOnClickListener(this)
            get_reservation_button.setOnClickListener(this)
            reservationAdapter = ReservationAdapter(fragmentBaseActivity,viewModel)
            reservation_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(fragmentBaseActivity)
                adapter = reservationAdapter
            }

            strtDateCalendar = Calendar.getInstance()
            endDateCalendar = Calendar.getInstance()
            start_date_button_search.text  = Util.getSelectedDateWithTime(strtDateCalendar!!)
            end_date_button_search.text  = Util.getSelectedDateWithTime(endDateCalendar!!)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callService(){
        try{
            var reservationRequest = ReservationRequest()
            reservationRequest.search_type = "Search"
            if(!Validation.isOnline(fragmentBaseActivity)){
                fragmentBaseActivity.showSnackBar(progress_bar,getString(R.string.internet_connected))
                return
            }else{
                reservationRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                reservationRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                reservationRequest.datefrom =start_date_button_search.text.toString()!!
                reservationRequest.dateto =end_date_button_search.text.toString()!!
                viewModel.getReservationResponse(reservationRequest)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    fun startDateButton(){
        try{
            println("dataclick>>>>>>>>>>>>>")
            var calendar = Calendar.getInstance()
            MaterialDialog(fragmentBaseActivity).show {
                datePicker(null,null,calendar) { _, date ->
                    date.set(date.get(Calendar.YEAR),date.get(Calendar.MONTH),date.get(Calendar.DATE),0,0,0)
                    strtDateCalendar = date
                    var dateText = Util.getSelectedDateWithTime(date)
                    //Util.getSelectedDate(date)?.let { fragmentBaseActivity.showToast(it) }
                    activity!!.start_date_button_search.text = dateText

                }

            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    fun endDateButton(){
        try{

            println("dataclick>>>>>>>>>>>>>")
            var calendar = Calendar.getInstance()
            var futureCalendar = Calendar.getInstance()
            futureCalendar.set(futureCalendar.year,futureCalendar.month+1,futureCalendar.dayOfMonth)
            println("time>>> ${calendar.time}")
            MaterialDialog(fragmentBaseActivity).show {
                datePicker(null,futureCalendar,calendar) { _, date ->
                    if(strtDateCalendar !=null){
                        if(strtDateCalendar!!.before(date) || strtDateCalendar!! == date){
                            date.set(date.get(Calendar.YEAR),date.get(Calendar.MONTH),date.get(Calendar.DATE),23,59,59)
                            endDateCalendar = date
                            var dateText = Util.getSelectedDateWithTime(date)
                            //Util.getSelectedDate(date)?.let { fragmentBaseActivity.showToast(it) }
                            activity!!.end_date_button_search.text = dateText
                        }else{
                            fragmentBaseActivity.showSnackBar(progress_bar,"start-Date should be equal or less then end-Date")
                        }
                    }else{
                        fragmentBaseActivity.showSnackBar(progress_bar,"Please select start-Date")
                    }
                }
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
                    //callStatusService(reserStatusRequest)
                }
            } )
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

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { fragmentBaseActivity.showSnackBar(progress_bar,it) }
        })
        viewModel.response.observe(this, Observer<ReservationResponse> {
            reservationAdapter.notifyDataSetChanged()
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
                if(it.status == Constant.STATUS.SUCCESS){
                    fragmentBaseActivity.showToast(it.result!!)
                    callService()
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PandingFragment.
         */
        val TAG:String = PendingFragment::class.java.simpleName
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PendingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(view: View?) {
        try {
            when(view){
                start_date_layout->{
                    startDateButton()
                }
                end_date_layout->{
                    endDateButton()
                }
                start_date_button_search->{
                    startDateButton()
                }
                end_date_button_search->{
                    endDateButton()
                }
                get_reservation_button->{
                    callService()
                }
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
}
