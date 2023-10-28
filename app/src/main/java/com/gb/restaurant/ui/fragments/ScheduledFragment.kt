package com.gb.restaurant.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp

import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.FragmentScheduledBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.order.OrderRequest
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.ScheduleAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.OrderViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ScheduledFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ScheduledFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ScheduledFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var stopListener: OnStopListener? = null
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var list:MutableList<String> = ArrayList()
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: OrderViewModel
    private var _binding: FragmentScheduledBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        for(i in 0..20){
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

        _binding = FragmentScheduledBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scheduleAdapter = ScheduleAdapter(fragmentBaseActivity,viewModel)
        binding.scheduleRecycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(fragmentBaseActivity)
            adapter = scheduleAdapter
        }


        attachObserver()

        callService()

        binding.scheduleSwipeRefresh.setOnRefreshListener {
            callService()
        }
    }

    private fun callService(){
        try{
            if(Validation.isOnline(fragmentBaseActivity)){
                var orderRequest = OrderRequest()
                orderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderRequest.service_type = Constant.SERVICE_TYPE.GET_HOLD_ORDER
                orderRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                viewModel.getOrderResponse(orderRequest, false)
            }else{
                fragmentBaseActivity.showSnackBar(binding.progressBar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(viewLifecycleOwner, Observer<String> {
            binding.scheduleSwipeRefresh.isRefreshing = false
            it?.let { fragmentBaseActivity.showSnackBar(binding.progressBar,it) }
        })
        viewModel.orderResponse.observe(viewLifecycleOwner, Observer<OrderResponse> {
            binding.scheduleSwipeRefresh.isRefreshing = false
            it?.let {
                scheduleAdapter.notifyDataSetChanged()
                if(scheduleAdapter.itemCount >0){
                    binding.scheduleRecycler.visibility = View.VISIBLE
                    binding.noOrderText.visibility = View.GONE
                  //  onButtonPressed(Constant.TAB.SCHEDULE,scheduleAdapter.itemCount)
                }else{
                    binding.scheduleRecycler.visibility = View.GONE
                    binding.noOrderText.visibility = View.VISIBLE
                }
                //stopListener?.onStop(Constant.TAB.SCHEDULE,scheduleAdapter.itemCount)
            }
        })


    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(position:Int,count:Int) {
        listener?.onFragmentInteraction(position,count)
    }
    interface OnStopListener {
        // TODO: Update argument type and name
        fun onStop(fragment: Int,count:Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
        if (context is OnStopListener) {
            stopListener=context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        stopListener=null
    }
    override fun onResume() {
        super.onResume()
        scheduleAdapter.setOnItemClickListener(object :ScheduleAdapter.NewOrClickListener{
            override fun onItemClick(position:Int, v: View) {

            }

        } )
        callService()
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
        fun onFragmentInteraction(position: Int,count:Int)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ScheduledFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ScheduledFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private val TAG = ScheduledFragment::class.java.simpleName
    }

    private fun createViewModel(): OrderViewModel =
        ViewModelProviders.of(this).get(OrderViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }
}
