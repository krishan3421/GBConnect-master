package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.ActivityViewInvoiceBinding
import com.gb.restaurant.databinding.ActivityViewReportBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.report.ReportRequest
import com.gb.restaurant.model.report.ReportResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.ViewReportAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReportViewModel


class ViewReportActivity : BaseActivity() {

    private lateinit var viewReportAdapter: ViewReportAdapter
    private var list:MutableList<String> = ArrayList()
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: ReportViewModel
    private lateinit var binding: ActivityViewReportBinding
    companion object{
        private val TAG:String = ViewReportActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_view_report)
        binding = ActivityViewReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }
    private fun initData(){
        rsLoginResponse = MyApp.instance.rsLoginResponse
        for(i in 0..20){
            list.add("$i")
        }
        viewModel = createViewModel()
    }
    private fun initView(){
        try{
            setSupportActionBar(binding.toolbar)
            binding.toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
            binding.toolbar.title = "${rsLoginResponse?.data?.name}"
            attachObserver()
            viewReportAdapter = ViewReportAdapter(this,list)
            binding.contentViewReport.apply {
                viewReportRecycler.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@ViewReportActivity)
                    adapter = viewReportAdapter
                }
                callService()

                reportsSwipeRefresh.setOnRefreshListener {
                    callService()
                }
            }

        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }



    private fun callService(){
        try{
            if(Validation.isOnline(this)){
                var reportRequest = ReportRequest()
                reportRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                reportRequest.deviceversion = Util.getVersionName(this)
                println("request date>>>>>> ${Util.getStringFromBean(reportRequest)}")
                viewModel.getReportResponse(reportRequest)
            }else{
                binding.contentViewReport.reportsSwipeRefresh.isRefreshing = false
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()
        viewReportAdapter.setOnItemClickListener(object :ViewReportAdapter.ReportClickListener{
            override fun onItemClick(position:Int, v: View) {

            }

        } )
    }


    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            binding.contentViewReport.reportsSwipeRefresh.isRefreshing = false
            it?.let { showSnackBar(binding.progressBar,it) }
        })
        viewModel.reportResponse.observe(this, Observer<ReportResponse> {
            binding.contentViewReport.reportsSwipeRefresh.isRefreshing = false
            it?.let {
                viewReportAdapter.notifyDataSetChanged()
                if(viewReportAdapter.itemCount >0){
                    binding.contentViewReport.viewReportRecycler.visibility = View.VISIBLE
                    binding.contentViewReport.noOrderText.visibility = View.GONE}
                else{
                    binding.contentViewReport.viewReportRecycler.visibility = View.GONE
                    binding.contentViewReport.noOrderText.visibility = View.VISIBLE
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

}
