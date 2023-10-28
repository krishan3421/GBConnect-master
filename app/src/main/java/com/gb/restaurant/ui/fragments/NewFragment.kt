package com.gb.restaurant.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gb.restaurant.CATPrintSDK.Canvas
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.FragmentMonthlyInvoiceBinding
import com.gb.restaurant.databinding.FragmentNewBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.model.order.OrderRequest
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.NewDetailActivity
import com.gb.restaurant.ui.OrdersActivity
import com.gb.restaurant.ui.adapter.NewAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.utils.Utils
import com.gb.restaurant.viewmodel.OrderViewModel
import com.gb.restaurant.session.SessionManager


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [NewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [NewFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class NewFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var stopListener: OnStopListener? = null
    private var reservationListener: OnReservListener? = null
    private lateinit var orderAdapter: NewAdapter
    private var list: MutableList<String> = ArrayList()
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: OrderViewModel
    private var orderContext: OrdersActivity? = null

    public var mCanvas: Canvas? = null
    public var canvasBitmap: Bitmap? = null
    public var mBitmap: Bitmap? = null

    var sessionManager: SessionManager? = null

    var confirmData:Data?=null
    private var _binding: FragmentNewBinding? = null
    private val binding get() = _binding!!
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is OrdersActivity)
            orderContext = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this;
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
        println("page call>>>>>>>>>>>fragment_new")
        _binding = FragmentNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            sessionManager = SessionManager(requireContext())

            orderAdapter = NewAdapter(fragmentBaseActivity as OrdersActivity, viewModel)
            binding.newOrderRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(fragmentBaseActivity)
                adapter = orderAdapter
            }

            attachObserver()

            callService(false)

            binding.newSwipeRefresh.setOnRefreshListener {
                callService(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun callService(isPrintLastOrder: Boolean) {
        try {
            if (Validation.isOnline(fragmentBaseActivity)) {
                // fragmentBaseActivity.showToast("broadcast new")
                var orderRequest = OrderRequest()
                orderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderRequest.service_type =
                    Constant.SERVICE_TYPE.GET_NEW_ORDER//Constant.SERVICE_TYPE.GET_NEW_ORDER
                orderRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                // println("new request>>>>> ${Util.getStringFromBean(orderRequest)}")
                viewModel.getOrderResponse(orderRequest, isPrintLastOrder)
            } else {
                fragmentBaseActivity.showSnackBar(
                    binding.progressBar,
                    getString(R.string.internet_connected)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    /*fun callService(context:Context=fragmentBaseActivity,restId:String=rsLoginResponse?.data?.restaurantId!!){
        try{
            if(Validation.isOnline(context)){
                var orderRequest = OrderRequest()
                orderRequest.restaurant_id = restId
                orderRequest.service_type = Constant.SERVICE_TYPE.GET_NEW_ORDER//Constant.SERVICE_TYPE.GET_NEW_ORDER
                orderRequest.deviceversion = Util.getVersionName(context)
                // println("new request>>>>> ${Util.getStringFromBean(orderRequest)}")
                viewModel.getOrderResponse(orderRequest)
            }else{
                fragmentBaseActivity.showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message)
        }
    }*/

    private fun attachObserver() {
        viewModel.isLoading.observe(fragmentBaseActivity, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(fragmentBaseActivity, Observer<String> {
            if (binding.newSwipeRefresh != null)
                binding.newSwipeRefresh.isRefreshing = false
            it?.let { fragmentBaseActivity.showSnackBar(binding.progressBar, it) }
        })
        viewModel.orderResponse.observe(fragmentBaseActivity, Observer<OrderResponse> {
            if (binding.newSwipeRefresh != null)
                binding.newSwipeRefresh.isRefreshing = false
            it?.let {
                orderAdapter.notifyDataSetChanged()
                if (orderAdapter.itemCount > 0) {
                    binding.newOrderRecycler?.visibility = View.VISIBLE
                    binding.noOrderText?.visibility = View.GONE
                    orderAdapter?.itemCount?.let { it1 -> onButtonPressed(Constant.TAB.NEW, it1) }

                } else {
                    binding.newOrderRecycler?.visibility = View.GONE
                    binding.noOrderText?.visibility = View.VISIBLE
                }
                var reservationCount = it.reservation ?: 0
                reservationListener?.onStartStop(reservationCount)
            }
        })

        viewModel.printLastOrder.observe(fragmentBaseActivity, Observer<Boolean> {
            stopListener?.onStop(Constant.TAB.NEW, orderAdapter.itemCount)
            it?.let {

                if (it) {
                    if (checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), 1
                        )
                    } else {
                        if (Utils.isLocationEnabled(context)!!) {
                            if (sessionManager!!.getPrinterAddress().isNotEmpty()) {
                                Handler().postDelayed({
                                    print(
                                        sessionManager!!.getPrinterType(),
                                        sessionManager!!.getPrinterAddress(),
                                        viewModel.getOrderAt(0)
                                    )
                                }, 5*1000)
                            }
                            stopListener?.onStop(Constant.TAB.NEW, orderAdapter.itemCount)
                        }

                    }

                }


            }
        })



        viewModel.orderStatusResponse.observe(viewLifecycleOwner, Observer<OrderStatusResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                    fragmentBaseActivity.showToast(it.result!!)
                } else {
                    fragmentBaseActivity.showToast(it.result!!)
                    callService(false)
                    (activity as OrdersActivity?)!!.refreshActiveFragment()

                }
            }
        })


    }


    private fun confirmNewOrder(orderId: String, orderType: String) {
        var orderStatusRequest = OrderStatusRequest()
        orderStatusRequest.deviceversion = Util.getVersionName(requireContext())
        orderStatusRequest.status = Constant.ORDER_STATUS.CONFIRMED
        orderStatusRequest.order_id = orderId
        if (orderType.equals("Delivery", true)) {
            orderStatusRequest.readytime = "${rsLoginResponse?.data?.deliverytime?.get(0)} minutes"
        } else {
            orderStatusRequest.readytime = "${rsLoginResponse?.data?.pickuptime?.get(0)} minutes"
        }

        try {
            if (Validation.isOnline(requireContext())) {
                orderStatusRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                println("request>>>>> ${Util.getStringFromBean(orderStatusRequest)}")
                viewModel.orderStatus(orderStatusRequest)
            } else {
                fragmentBaseActivity.showToast(getString(R.string.internet_connected))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }

    }


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
        if (context is OnStopListener) {
            stopListener = context
        } else {
            throw RuntimeException("$context must implement OnStopListener")
        }
        if (context is OnReservListener) {
            reservationListener = context
        } else {
            throw RuntimeException("$context must implement OnReservationListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        stopListener = null
    }

    override fun onResume() {
        super.onResume()
        fragmentBaseActivity?.registerReceiver(printStatusBroadcast,
            IntentFilter("com.gb.restaurant.utils.returnPrintStatus")
        );
        Utils.setBluetooth(true,fragmentBaseActivity)

        orderAdapter.setOnItemClickListener(object : NewAdapter.NewOrClickListener {
            override fun onItemClick(data: Data, position: Int, v: View) {
                MyApp.instance.data = data

                if (v.id == R.id.view_layout) {
                    var intent = Intent(activity, NewDetailActivity::class.java)
                    //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra(NewDetailActivity.FROMPAGE, 0)
                    startActivityForResult(intent, NEW_DETAIL_PAGE)
                }


            }

        })
    }

    override fun onPause() {
        super.onPause()
        fragmentBaseActivity?.unregisterReceiver(printStatusBroadcast);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == NEW_DETAIL_PAGE) {
                    //callService()
                    orderContext?.orderViewPager?.adapter?.notifyDataSetChanged()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
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

    interface OnStopListener {
        // TODO: Update argument type and name
        fun onStop(fragment: Int, count: Int)
    }

    interface OnReservListener {
        // TODO: Update argument type and name
        fun onStartStop(count: Int)
    }

    companion object {

        private var instance: NewFragment? = null

        @Synchronized
        fun getInstance(): NewFragment? {
            return instance
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewFragment.
         */
        // TODO: Rename and change types and number of parameters
        val NEW_DETAIL_PAGE: Int = 22

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun createViewModel(): OrderViewModel =
        ViewModelProviders.of(this).get(OrderViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) binding.progressBar?.visibility = View.VISIBLE else binding.progressBar?.visibility = View.GONE
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (sessionManager!!.getPrinterAddress().isNotEmpty()) {
                    print(
                        sessionManager!!.getPrinterType(),
                        sessionManager!!.getPrinterAddress(),
                        viewModel.getOrderAt(0)
                    )
                }
            }
        }
    }


    fun print(printer_type: Int, printer_id: String, data: Data?) {

        mCanvas = Canvas(canvasBitmap)
        mBitmap = Utils.createOrderReceipt(context, mCanvas, 576, data)
        if (mBitmap != null) {
            confirmData = data
            //Print Munbyn
          Utils.munbynPrinting(
                context,
                mBitmap,
                printer_type,
                printer_id
            )


        }

    }
    private val printStatusBroadcast:BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1?.let {
                println("printStatusBroadcast>>>>")
                val status = it.getIntExtra("PRINT_STATUS",0)
                println("printStatusBroadcast>>>> $status")
                if(status==1){
                    confirmNewOrder("" + confirmData!!.orderid, "" + confirmData!!.type!!)
                }
            }

        }
    }

}
