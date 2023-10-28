package com.gb.restaurant.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.FragmentMonthlyMealBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.report.ReportRequest
import com.gb.restaurant.model.report.ReportResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.MonthlyMealAdapter
import com.gb.restaurant.utils.ListPaddingDecorationGray
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReportViewModel
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MonthlyMealFragment : BaseFragment() ,View.OnClickListener{

    private lateinit var monthlyMealAdapter: MonthlyMealAdapter
    private var list:MutableList<String> = ArrayList()
    private lateinit var viewModel: ReportViewModel
    private var monthIndex = 0
    private var yearIndex = 0
    private var selectMonth = "01"
    private var selectYear = ""
    private var yearArray = mutableListOf<String>()
    private var monthArray = mutableListOf<String>("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    var rsLoginResponse: RsLoginResponse? = null
    private var _binding: FragmentMonthlyMealBinding? = null
    private val binding get() = _binding!!
    companion object{
        private val TAG = MonthlyMealFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectMonth =  Util.getCurrentMonth()!!
        selectYear = Util.getCurrentYear()!!
        rsLoginResponse = MyApp.instance.rsLoginResponse
        var year = Util.getCurrentYearInt()!!
        yearArray.add(year.toString())
        year--
        yearArray.add(year.toString())
        viewModel = createViewModel()
        monthArray.forEachIndexed { index, s ->
            if(Util.getMonth() ==s){
                monthIndex = index
            }
        }
        yearArray.forEachIndexed { index, s ->
            if(Util.getCurrentYear() ==s){
                yearIndex = index
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMonthlyMealBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try{
            attachObserver()
            binding.apply {
                monthButton.setOnClickListener(this@MonthlyMealFragment)
                yearButton.setOnClickListener(this@MonthlyMealFragment)
                monthButton.text = monthArray[monthIndex]
                yearButton.text = selectYear
            }

            monthlyMealAdapter = MonthlyMealAdapter(fragmentBaseActivity,viewModel)
            binding.monthlyMealRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(fragmentBaseActivity)
                adapter = monthlyMealAdapter
                addItemDecoration(ListPaddingDecorationGray(fragmentBaseActivity, 0,0))
            }
            callService()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }

    }

    private fun callService(){
        try{
            if(Validation.isOnline(fragmentBaseActivity)){
                var reportRequest = ReportRequest()
                reportRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                reportRequest.month = selectMonth
                reportRequest.year = selectYear
                reportRequest.deviceversion = Util.getVersionName(fragmentBaseActivity)
                println("request date>>>>>> ${Util.getStringFromBean(reportRequest)}")
                viewModel.getReportResponse(reportRequest)
            }else{
                //reports_swipe_refresh.isRefreshing = false
                fragmentBaseActivity.showSnackBar(binding.progressBar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }



    private fun openMonth(v:View){
        try{
            MaterialDialog(fragmentBaseActivity).show {
                title(R.string.month)
                listItemsSingleChoice(null,monthArray, initialSelection = monthIndex) { _, index, text ->
                    monthIndex = index
                    try{
                        binding.monthButton.text = text
                        when(text){
                            "Jan"->{
                                selectMonth = "01"
                            }
                            "Feb"->{
                                selectMonth = "02"
                            }
                            "Mar"->{
                                selectMonth = "03"
                            }
                            "Apr"->{
                                selectMonth = "04"
                            }
                            "May"->{
                                selectMonth = "05"
                            }
                            "Jun"->{
                                selectMonth = "06"
                            }
                            "Jul"->{
                                selectMonth = "07"
                            }
                            "Aug"->{
                                selectMonth = "08"
                            }
                            "Sep"->{
                                selectMonth = "09"
                            }"Oct"->{
                            selectMonth = "10"
                        }"Nov"->{
                            selectMonth = "11"
                        }"Dec"->{
                            selectMonth = "12"
                        }
                        }
                        callService()
                    }catch (e:Exception){
                        e.printStackTrace()
                        Log.e(TAG,e.message!!)
                    }

                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun openYear(v:View){
        try{
            MaterialDialog(fragmentBaseActivity).show {
                title(R.string.month)
                listItemsSingleChoice(null,yearArray, initialSelection = yearIndex) { _, index, text ->
                    yearIndex = index
                    try{
                       binding.yearButton.text = text
                        selectYear = text
                        callService()
                    }catch (e:Exception){
                        e.printStackTrace()
                        Log.e(TAG,e.message!!)
                    }

                }
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
        viewModel.apiError.observe(viewLifecycleOwner, androidx.lifecycle.Observer<String> {
            it?.let { fragmentBaseActivity.showSnackBar(binding.progressBar,it) }
        })
        viewModel.reportResponse.observe(viewLifecycleOwner, Observer<ReportResponse> {
            it?.let {
                monthlyMealAdapter.notifyDataSetChanged()
                if(monthlyMealAdapter.itemCount >0){
                    binding.monthlyMealRecycler.visibility = View.VISIBLE
                    binding.noOrderText.visibility = View.GONE

                }else{
                    binding.monthlyMealRecycler.visibility = View.GONE
                    binding.noOrderText.visibility = View.VISIBLE
                }
            }
        })


    }

    private fun createViewModel(): ReportViewModel =
        ViewModelProviders.of(this).get(ReportViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

    override fun onClick(view: View?) {
        try{
            when(view){
                binding.yearButton->{
                    openYear(binding.yearButton)
                }
                binding.monthButton->{
                   openMonth(binding.monthButton)
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
}
