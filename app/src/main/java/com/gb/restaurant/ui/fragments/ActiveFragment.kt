package com.gb.restaurant.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.gb.restaurant.CATPrintSDK.Canvas
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.PrinterModel
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.model.order.OrderRequest
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.HomeDetailActivity
import com.gb.restaurant.ui.OrdersActivity
import com.gb.restaurant.ui.adapter.ActiveAdapter
import com.gb.restaurant.ui.adapter.PrinterListAdapter
import com.gb.restaurant.utils.BluetoothDiscovery
import com.gb.restaurant.utils.Util
import com.gb.restaurant.utils.Utils
import com.gb.restaurant.viewmodel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_active.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ActiveFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ActiveFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ActiveFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var activeAdapter: ActiveAdapter
    private var stopListener: OnStopListener? = null
    private var list: MutableList<String> = ArrayList()
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: OrderViewModel
    private var materialDialog: MaterialDialog? = null
    private var orderContext: OrdersActivity? = null

    public var mCanvas: Canvas? = null
    public var canvasBitmap: Bitmap? = null
    public var mBitmap: Bitmap? = null

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
        println("page call>>>>>>>>>>>fragment_active")
        return inflater.inflate(R.layout.fragment_active, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activeAdapter = ActiveAdapter(fragmentBaseActivity, viewModel)
        active_recycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(fragmentBaseActivity)
            adapter = activeAdapter
        }
        attachObserver()

        callService()

        active_swipe_refresh.setOnRefreshListener {
            callService()
        }
    }

    fun callService() {
        try {
            if (Validation.isOnline(fragmentBaseActivity)) {
                var orderRequest = OrderRequest()
                orderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderRequest.service_type = Constant.SERVICE_TYPE.GET_ACTIVE_ORDER
                orderRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                println("activeresponse>>> ${Util.getStringFromBean(orderRequest)}")
                viewModel.getOrderResponse(orderRequest, false)
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

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(position: Int, count: Int) {
        listener?.onFragmentInteraction(position, count)
    }

    interface OnStopListener {
        // TODO: Update argument type and name
        fun onStop(fragment: Int, count: Int)
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(position: Int, count: Int)
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
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        stopListener = null
    }

    override fun onResume() {
        super.onResume()

        Utils.setBluetooth(true)

        activeAdapter.setOnItemClickListener(object : ActiveAdapter.NewOrClickListener {
            override fun onItemClick(data: Data, postion: Int, v: View) {
                MyApp.instance.data = data
                if (v.id == R.id.view_layout) {
                    var intent = Intent(fragmentBaseActivity, HomeDetailActivity::class.java)
                    intent.putExtra(HomeDetailActivity.FROMPAGE, 1)
                    startActivityForResult(intent, DETAIL_PAGE)
                }


            }

        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == DETAIL_PAGE) {
                    println("item_or tip>>>>>>>>>>>")
                    callService()
                    //orderContext?.orderViewPager?.adapter?.notifyDataSetChanged()
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
    // TODO: Rename method, update argument and hook method into UI event

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            if (active_swipe_refresh != null)
                active_swipe_refresh.isRefreshing = false
            it?.let { fragmentBaseActivity.showSnackBar(progress_bar, it) }
        })
        viewModel.orderResponse.observe(this, Observer<OrderResponse> {
            if (active_swipe_refresh != null)
                active_swipe_refresh.isRefreshing = false
            it?.let {
                println("data>>> ${Util.getStringFromBean(it)}")
                activeAdapter.notifyDataSetChanged()
                if (activeAdapter.itemCount > 0) {
                    active_recycler?.visibility = View.VISIBLE
                    no_order_text?.visibility = View.GONE
                    onButtonPressed(Constant.TAB.ACTIVE, activeAdapter.itemCount)

                } else {
                    active_recycler?.visibility = View.GONE
                    no_order_text?.visibility = View.VISIBLE
                }
                stopListener?.onStop(Constant.TAB.ACTIVE, activeAdapter.itemCount)
            }
        })

        viewModel.orderStatusResponse.observe(this, Observer<OrderStatusResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                    fragmentBaseActivity.showToast(it.result!!)
                } else {
                    fragmentBaseActivity.showToast(it.result!!)
                    if (materialDialog != null) {
                        materialDialog!!.dismiss()
                    }
                    callService()
                }
            }
        })


    }


    companion object {
        private var instance: ActiveFragment? = null

        @Synchronized
        fun getInstance(): ActiveFragment? {
            return instance
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ActiveFragment.
         */
        // TODO: Rename and change types and number of parameters
        const val DETAIL_PAGE = 26

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActiveFragment().apply {
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
        if (show) progress_bar?.visibility = View.VISIBLE else progress_bar?.visibility = View.GONE
    }
}
