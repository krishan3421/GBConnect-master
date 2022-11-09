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
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.report.ReportRequest
import com.gb.restaurant.model.report.ReportResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.ViewReportAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReportViewModel
import kotlinx.android.synthetic.main.activity_view_report.*
import kotlinx.android.synthetic.main.content_view_report.*


class ViewReportActivity : BaseActivity() {

    private lateinit var viewReportAdapter: ViewReportAdapter
    private var list:MutableList<String> = ArrayList()
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: ReportViewModel

    companion object{
        private val TAG:String = ViewReportActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_report)
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
            setSupportActionBar(toolbar)
            toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            toolbar.title = "${rsLoginResponse?.data?.name}"
            attachObserver()
            viewReportAdapter = ViewReportAdapter(this,list)
            view_report_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@ViewReportActivity)
                adapter = viewReportAdapter
            }
            callService()

            reports_swipe_refresh.setOnRefreshListener {
                callService()
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
                reports_swipe_refresh.isRefreshing = false
                showSnackBar(progress_bar,getString(R.string.internet_connected))
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
            reports_swipe_refresh.isRefreshing = false
            it?.let { showSnackBar(progress_bar,it) }
        })
        viewModel.reportResponse.observe(this, Observer<ReportResponse> {
            reports_swipe_refresh.isRefreshing = false
            it?.let {
                viewReportAdapter.notifyDataSetChanged()
                if(viewReportAdapter.itemCount >0){
                    view_report_recycler.visibility = View.VISIBLE
                    no_order_text.visibility = View.GONE}
                else{
                    view_report_recycler.visibility = View.GONE
                    no_order_text.visibility = View.VISIBLE
                }
            }
        })


    }

    private fun createViewModel(): ReportViewModel =
        ViewModelProviders.of(this).get(ReportViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

}
