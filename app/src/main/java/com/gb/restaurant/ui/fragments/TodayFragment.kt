package com.gb.restaurant.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
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
import com.gb.restaurant.databinding.FragmentSearchBinding
import com.gb.restaurant.databinding.FragmentTodayBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.reservation.*
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.status.ReserStatusRequest
import com.gb.restaurant.model.status.StatusResponse
import com.gb.restaurant.ui.adapter.ReservationAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReservationViewModel
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
    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            binding.stopReservationButton.setOnClickListener(this)

            reservationAdapter = ReservationAdapter(fragmentBaseActivity,viewModel)
            binding.reservationRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(fragmentBaseActivity)
                adapter = reservationAdapter
            }
            binding.reservationSwipeRefresh.setOnRefreshListener {
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
                binding.reservationSwipeRefresh.isRefreshing = false
                fragmentBaseActivity.showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
                fragmentBaseActivity.showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
            binding.reservationSwipeRefresh.isRefreshing = false
            it?.let { fragmentBaseActivity.showSnackBar(binding.progressBar,it) }
        })
        viewModel.response.observe(this, Observer<ReservationResponse> {
            reservationAdapter.notifyDataSetChanged()
            binding.reservationSwipeRefresh.isRefreshing = false
            if(reservationAdapter.itemCount >0){
                binding.reservationRecycler.visibility = View.VISIBLE
                binding.noOrderText.visibility = View.GONE

            }else{
                binding.reservationRecycler.visibility = View.GONE
                binding.noOrderText.visibility = View.VISIBLE
            }
        })
        viewModel.resStatusResponse.observe(this, Observer<StatusResponse> {
            it?.let {
                println("status response>>>>>>> ${Util.getStringFromBean(it)}")
                if(it.status == Constant.STATUS.SUCCESS){
                    fragmentBaseActivity.showToast(it.result?:"")
                    initCallService()
                }else{
                    fragmentBaseActivity.showToast(it.result?:"")
                }
            }
        })

        viewModel.stopReserResponse.observe(this, Observer<StopReservationResponse> {
            it?.let {
                stopDialog?.findViewById<ProgressBar>(R.id.stop_progress_bar)?.visibility =View.GONE
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
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

    fun startDateTimePopUp(startDate:Boolean=false){
        try{
            MaterialDialog(fragmentBaseActivity).show {
                title(text = "Select Date and Time")
                dateTimePicker(requireFutureDateTime = true,show24HoursView=true) { _, dateTime ->
                    dateTime.set(dateTime.get(Calendar.YEAR),dateTime.get(Calendar.MONTH),dateTime.get(Calendar.DATE),
                        dateTime.get(Calendar.HOUR_OF_DAY),dateTime.get(Calendar.MINUTE),dateTime.get(Calendar.SECOND))
                    if(startDate){
                        stopDialog?.findViewById<TextView>(R.id.from_date_text)?.text = Util.getMMM_DD_YYYY(dateTime.time)
                    }else{
                        val selectDateText = stopDialog?.findViewById<TextView>(R.id.select_date_text)
                        selectDateText?.visibility = View.VISIBLE
                        selectDateText?.text = Util.getMMM_DD_YYYY(dateTime.time)
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
                    stopDialog?.findViewById<TextView>(R.id.to_date_text)?.text = Util.getMMM_DD_YYYY(dateTime.time)
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
                binding.stopReservationButton->{
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
                val titleStopText = this.findViewById<TextView>(R.id.title_stop_text);
                val fromDateText = this.findViewById<TextView>(R.id.from_date_text);
                val toDateText = this.findViewById<TextView>(R.id.to_date_text);
                val radioGroup = this.findViewById<RadioGroup>(R.id.radio_group);
                val stopTodayRadio = this.findViewById<RadioButton>(R.id.stop_today_radio);
                val selectDateRadio = this.findViewById<RadioButton>(R.id.select_date_radio);
                val stopBetweenRadio = this.findViewById<RadioButton>(R.id.stop_betwwen_radio);
                val selectDateText = this.findViewById<TextView>(R.id.select_date_text);
                val fromToDateLayout = this.findViewById<LinearLayout>(R.id.from_to_date_layout);
                val stopProgressBar = this.findViewById<ProgressBar>(R.id.stop_progress_bar);
                val confirmButtonStop = this.findViewById<Button>(R.id.confirm_button_stop);
                val closeDialogStop = this.findViewById<ImageView>(R.id.close_dialog_stop);
                titleStopText.text = "Stop Reservation"
                stopDialog = this
                fromDateText.text = Util.getMMM_DD_YYYY(Date())
                toDateText.text = Util.getMMM_DD_YYYY(Date())
                fromDateText.setOnClickListener {
                    startDateTimePopUp(true)
                }
                toDateText.setOnClickListener {
                    toDateTimePopUp()
                }
                var radio: RadioButton =stopTodayRadio
                radioGroup.setOnCheckedChangeListener { radioGroup, checkedId  ->
                     radio = radioGroup.findViewById(checkedId)
                   when(radio){
                       stopTodayRadio->{
                           selectDateText?.visibility = View.INVISIBLE
                           fromToDateLayout.visibility = View.GONE
                          // date_layout.visibility = View.GONE
                       }
                       selectDateRadio->{
                           fromToDateLayout.visibility = View.GONE
                           startDateTimePopUp(false)
                           //date_layout.visibility = View.VISIBLE
                           //end_date_layout.visibility = View.GONE
                       }
                       stopBetweenRadio->{
                           fromToDateLayout.visibility = View.VISIBLE
                           selectDateText?.visibility = View.INVISIBLE
                           //date_layout.visibility = View.VISIBLE
                          // end_date_layout.visibility = View.VISIBLE
                       }
                   }
                }
                closeDialogStop.setOnClickListener {
                    this.dismiss()
                }
                confirmButtonStop.setOnClickListener {
                    var reservationStopRequest= ReservationStopRequest()
                    when(radio){
                        stopTodayRadio->{
                            reservationStopRequest.stop = Constant.RESERVATION_STOP.TODAY
                        }
                        selectDateRadio->{
                            reservationStopRequest.stop = Constant.RESERVATION_STOP.DAY
                            reservationStopRequest.datefrom =dateFrom
                        }
                        stopBetweenRadio->{
                            reservationStopRequest.stop = Constant.RESERVATION_STOP.BETWEEN
                            reservationStopRequest.datefrom =dateFrom
                            reservationStopRequest.dateto =dateTo
                        }
                    }
                    stopProgressBar.visibility =View.VISIBLE
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
                fragmentBaseActivity.showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
            val titleDialogText = this.findViewById<TextView>(R.id.title_dialog_text);
            val peopleText = this.findViewById<TextView>(R.id.people_text);
            val dateText = this.findViewById<TextView>(R.id.date_text);
            val bookingDateText = this.findViewById<TextView>(R.id.booking_date_text);
            val locationText = this.findViewById<TextView>(R.id.location_text);
            val locationType = this.findViewById<TextView>(R.id.location_type);
            val desText = this.findViewById<TextView>(R.id.des_text);
            val replyEdittext = this.findViewById<EditText>(R.id.reply_edittext);
            val confirmButton = this.findViewById<Button>(R.id.confirm_button);
            val cancelButton = this.findViewById<Button>(R.id.cancel_button);
            val closeDialog = this.findViewById<ImageView>(R.id.close_dialog);
            val replyText = this.findViewById<TextView>(R.id.reply_text);
            val replyTextVal = this.findViewById<TextView>(R.id.reply_text_val);
            titleDialogText.text = "${data.name?:""}"
            peopleText.text ="Peoples: ${data.peoples}"
            dateText.text ="Date: ${data.date2}"
            bookingDateText.text ="Booking Date: ${data.bookingtime}"
            if(data.location.isNullOrEmpty()){
                locationText.visibility = View.GONE
                locationType.visibility = View.GONE
            }else{
                locationText.text ="${data.location}"
            }

            desText.text = "${data.details}"
            if(data.reply.isNullOrEmpty()){
                if(data.status.equals(Constant.ORDER_STATUS.CLOSED,true)){
                    replyEdittext.visibility = View.GONE
                    confirmButton.visibility=View.GONE
                    cancelButton.text = "${data.status}"
                    cancelButton.alpha = 0.5f
                    cancelButton.isClickable=false
                    cancelButton.isEnabled=false
                }else if(data.status.equals(Constant.ORDER_STATUS.CONFIRMED,true)){
                    replyEdittext.visibility = View.GONE
                    confirmButton.text = "${data.status}"
                    confirmButton.alpha = 0.5f
                    confirmButton.isClickable=false
                    confirmButton.isEnabled=false
                    cancelButton.text = "CLOSE"
                }
                else if(data.status.equals(Constant.ORDER_STATUS.CANCEL,true)){
                    replyEdittext.visibility = View.GONE
                    confirmButton.visibility=View.GONE
                    cancelButton.text = "CANCELED"
                    cancelButton.alpha = 0.5f
                    cancelButton.isClickable=false
                    cancelButton.isEnabled=false
                }else{
                    replyEdittext.visibility = View.VISIBLE
                }
                replyText.visibility = View.GONE
                replyTextVal.visibility = View.GONE
            }else{
                replyText.text = "${data.reply}"
                replyText.visibility = View.VISIBLE
                replyEdittext.visibility = View.GONE
                replyTextVal.visibility = View.VISIBLE
                if(data.status.equals(Constant.ORDER_STATUS.CLOSED,true)){
                    confirmButton.visibility=View.GONE
                    cancelButton.text = "${data.status}"
                    cancelButton.alpha = 0.5f
                    cancelButton.isClickable=false
                    cancelButton.isEnabled=false
                }else if(data.status.equals(Constant.ORDER_STATUS.CONFIRMED,true)){
                    confirmButton.text = "${data.status}"
                    confirmButton.alpha = 0.5f
                    confirmButton.isClickable=false
                    confirmButton.isEnabled=false
                    cancelButton.text = "CLOSE"
                }
                else if(data.status.equals(Constant.ORDER_STATUS.CANCEL,true)){
                    confirmButton.visibility=View.GONE
                    cancelButton.text = "CANCELED"
                    cancelButton.alpha = 0.5f
                    cancelButton.isClickable=false
                    cancelButton.isEnabled=false
                }
            }

            closeDialog.setOnClickListener {
                this.dismiss()
            }
            confirmButton.setOnClickListener {
                var reserStatusRequest= ReserStatusRequest()
                reserStatusRequest.reservation_id = data.id!!
                reserStatusRequest.reply = replyEdittext.text.toString()
                reserStatusRequest.status = Constant.ORDER_STATUS.CONFIRMED
                callStatusService(reserStatusRequest)
                this.dismiss()
            }
            cancelButton.setOnClickListener {
                var reserStatusRequest= ReserStatusRequest()
                reserStatusRequest.reservation_id = data.id!!
                reserStatusRequest.reply = replyEdittext.text.toString()
                if(cancelButton.text.toString().equals(Constant.ORDER_STATUS.CANCEL,true)) {
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
