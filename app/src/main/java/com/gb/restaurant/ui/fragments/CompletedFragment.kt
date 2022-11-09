package com.gb.restaurant.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.order.CompOrderRequest
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.ComDetailActivity
import com.gb.restaurant.ui.adapter.CompletedAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.utils.Utils
import com.gb.restaurant.viewmodel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_completed.*
import kotlinx.android.synthetic.main.fragment_completed.progress_bar
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CompletedFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CompletedFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class CompletedFragment : BaseFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var compAdapter: CompletedAdapter
    private var list: MutableList<String> = ArrayList()
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: OrderViewModel
    var strtDateCalendar: Calendar? = null
    var endDateCalendar: Calendar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        for (i in 0..20) {
            list.add("$i")
        }
        rsLoginResponse = MyApp.instance.rsLoginResponse
        viewModel = createViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        println("page call>>>>>>>>>>>fragment_completed")
        return inflater.inflate(R.layout.fragment_completed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        strtDateCalendar = Calendar.getInstance()
        endDateCalendar = Calendar.getInstance()
        startDateLayout.setOnClickListener(this)
        endDateLayout.setOnClickListener(this)
        get_order_button.setOnClickListener(this)
        start_date_text.text = Util.get_yyyy_mm_dd(strtDateCalendar!!)
        end_date_text.text = Util.get_yyyy_mm_dd(endDateCalendar!!)
        compAdapter = CompletedAdapter(fragmentBaseActivity, viewModel)
        comp_recycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(fragmentBaseActivity)
            adapter = compAdapter
        }

        attachObserver()

        callService()

        comp_swipe_refresh.setOnRefreshListener {
            callService()
        }
    }

    private fun callService() {
        try {
            if (Validation.isOnline(fragmentBaseActivity)) {
                var orderRequest = CompOrderRequest()
                orderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderRequest.service_type = Constant.SERVICE_TYPE.GET_ALL_ORDER
                orderRequest.date_from = start_date_text.text.toString()
                orderRequest.date_to = end_date_text.text.toString()
                orderRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                println("request>>>> ${Util.getStringFromBean(orderRequest)}")
                viewModel.getCompOrderResponse(orderRequest)
            } else {
                fragmentBaseActivity.showSnackBar(
                    progress_bar,
                    getString(R.string.internet_connected)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(fragmentBaseActivity, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(fragmentBaseActivity, Observer<String> {
            if (comp_swipe_refresh != null)
                comp_swipe_refresh.isRefreshing = false
            it?.let { fragmentBaseActivity.showSnackBar(progress_bar, it) }
        })
        viewModel.orderResponse.observe(fragmentBaseActivity, Observer<OrderResponse> {
            if (comp_swipe_refresh != null)
                comp_swipe_refresh.isRefreshing = false

            it?.let {
                compAdapter.notifyDataSetChanged()
                if (compAdapter.itemCount > 0) {
                    comp_recycler?.visibility = View.VISIBLE
                    no_order_text?.visibility = View.GONE

                } else {
                    comp_recycler?.visibility = View.GONE
                    no_order_text?.visibility = View.VISIBLE
                }
            }
        })


    }

    // TODO: Rename method, update argument and hook method into UI event
    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(position: Int, count: Int) {
        listener?.onFragmentInteraction(position, count)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()

        Utils.setBluetooth(true)


        compAdapter.setOnItemClickListener(object : CompletedAdapter.NewOrClickListener {
            override fun onItemClick(data: Data, position: Int, v: View) {
                MyApp.instance.data = data

                if (v.id == R.id.view_layout) {
                    var intent = Intent(context, ComDetailActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra(ComDetailActivity.FROMPAGE, 0)
                    startActivity(intent)

                }


            }

        })
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(position: Int, count: Int)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CompletedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CompletedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private val TAG = CompletedFragment::class.java.simpleName
    }

    private fun createViewModel(): OrderViewModel =
        ViewModelProviders.of(this).get(OrderViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar?.visibility = View.VISIBLE else progress_bar?.visibility = View.GONE
    }

    override fun onClick(p0: View?) {
        try {
            when (p0) {
                startDateLayout -> {
                    startDateMethod()
                }
                endDateLayout -> {
                    endDateMethod()
                }
                get_order_button -> {
                    callService()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun startDateMethod() {
        try {
            var calendar = Calendar.getInstance()
            MaterialDialog(fragmentBaseActivity).show {
                datePicker(null, null, calendar) { _, date ->
                    date.set(
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DATE),
                        0,
                        0,
                        0
                    )
                    strtDateCalendar = date
                    var dateText = Util.get_yyyy_mm_dd(date)
                    //Util.getSelectedDate(date)?.let { fragmentBaseActivity.showToast(it) }
                    fragmentBaseActivity.start_date_text.setText(dateText)

                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun endDateMethod() {
        try {
            var calendar = Calendar.getInstance()
            println("time>>> ${calendar.time}")
            MaterialDialog(fragmentBaseActivity).show {
                datePicker(null, null, calendar) { _, date ->
                    if (strtDateCalendar != null) {
                        if (strtDateCalendar!!.before(date) || strtDateCalendar!! == date) {
                            date.set(
                                date.get(Calendar.YEAR),
                                date.get(Calendar.MONTH),
                                date.get(Calendar.DATE),
                                23,
                                59,
                                59
                            )
                            endDateCalendar = date
                            var dateText = Util.get_yyyy_mm_dd(date)
                            //Util.getSelectedDate(date)?.let { fragmentBaseActivity.showToast(it) }
                            fragmentBaseActivity.end_date_text.setText(dateText)
                            //callService()
                        } else {
                            fragmentBaseActivity.showSnackBar(
                                fragmentBaseActivity.end_date_text,
                                "start-Date should be equal or less then end-Date"
                            )
                        }
                    } else {
                        fragmentBaseActivity.showSnackBar(
                            fragmentBaseActivity.end_date_text,
                            "Please select start-Date"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }


}
