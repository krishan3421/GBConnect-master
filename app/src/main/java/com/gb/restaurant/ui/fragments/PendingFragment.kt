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
import android.widget.TextView
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
import com.gb.restaurant.databinding.FragmentPandingBinding
import com.gb.restaurant.model.reservation.Data
import com.gb.restaurant.model.reservation.ReservationRequest
import com.gb.restaurant.model.status.ReserStatusRequest
import com.gb.restaurant.model.status.StatusResponse
import com.gb.restaurant.viewmodel.ReservationViewModel
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
class PendingFragment : BaseFragment(),View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var strtDateCalendar : Calendar?= null
    var endDateCalendar :Calendar?= null
    private lateinit var reservationAdapter: ReservationAdapter
    private var list:MutableList<String> = ArrayList()
    private lateinit var viewModel: ReservationViewModel
    var rsLoginResponse: RsLoginResponse? = null
    private var _binding: FragmentPandingBinding? = null
    private val binding get() = _binding!!
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentPandingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            binding.apply {
                endDateLayout.setOnClickListener(this@PendingFragment)
                startDateLayout.setOnClickListener(this@PendingFragment)
                startDateButton.setOnClickListener(this@PendingFragment)
                endDateButton.setOnClickListener(this@PendingFragment)
                getReservationButton.setOnClickListener(this@PendingFragment)
            }

            reservationAdapter = ReservationAdapter(fragmentBaseActivity,viewModel)
            binding.reservationRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(fragmentBaseActivity)
                adapter = reservationAdapter
            }

            strtDateCalendar = Calendar.getInstance()
            endDateCalendar = Calendar.getInstance()
            binding.startDateButton.text  = Util.getSelectedDateWithTime(strtDateCalendar!!)
            binding.endDateButton.text  = Util.getSelectedDateWithTime(endDateCalendar!!)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callService(){
        try{
            if(Validation.isOnline(fragmentBaseActivity)){
                var reservationRequest = ReservationRequest()
                reservationRequest.datefrom =binding.startDateButton.text.toString()!!
                reservationRequest.dateto =binding.endDateButton.text.toString()!!
                reservationRequest.search_type = "Pending"
                reservationRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                reservationRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                viewModel.getReservationResponse(reservationRequest)
            }else{
                fragmentBaseActivity.showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
                    binding.startDateButton.setText(dateText)

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
                            binding.endDateButton.setText(dateText)
                        }else{
                            fragmentBaseActivity.showSnackBar(binding.progressBar,"start-Date should be equal or less then end-Date")
                        }
                    }else{
                        fragmentBaseActivity.showSnackBar(binding.progressBar,"Please select start-Date")
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
                fragmentBaseActivity.showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
            it?.let { fragmentBaseActivity.showSnackBar(binding.progressBar,it) }
        })
        viewModel.response.observe(this, Observer<ReservationResponse> {
            reservationAdapter.notifyDataSetChanged()
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
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
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
            binding.apply {
                when(view){
                    startDateLayout->{
                        startDateButton()
                    }
                    endDateLayout->{
                        endDateButton()
                    }
                    startDateButton->{
                        startDateButton()
                    }
                    endDateButton->{
                        endDateButton()
                    }
                    getReservationButton->{
                        callService()
                    }
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
            titleDialogText.text =data.name?:""
            peopleText.text ="${data.peoples}"
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
}
