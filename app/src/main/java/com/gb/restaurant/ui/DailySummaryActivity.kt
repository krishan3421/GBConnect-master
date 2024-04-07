package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gb.restaurant.Constant
import com.gb.restaurant.databinding.ActivityDailySummaryBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.daily.DailySumRequest
import com.gb.restaurant.model.daily.DailySummResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.DailySummAdapter
import com.gb.restaurant.ui.adapter.GbSummAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.AddTipsViewModel
import java.util.*


class DailySummaryActivity : BaseActivity() {

    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: AddTipsViewModel
    private lateinit var dailyAdapter: DailySummAdapter
    private lateinit var gbSummAdapter: GbSummAdapter
    private lateinit var binding: ActivityDailySummaryBinding
    companion object{
        private val TAG = DailySummaryActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
       // setContentView(R.layout.activity_daily_summary)
        binding = ActivityDailySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }

    private fun statusBarTransparent(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
       /* if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }*/
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    private fun initData(){
        try{
            rsLoginResponse = MyApp.instance.rsLoginResponse
            viewModel = createViewModel()
           // println("date>>>>>> ${Util.getMM_day_yyyy(Date())}")
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initView(){
        try{
            binding.apply {
                customAppbar.backLayout.setOnClickListener {
                    onBackPressed()
                }
                contentDailySummary.dateButton.text = "${Util.getMM_day_yyyy(Date())}"
                attachObserver()
                dailyAdapter = DailySummAdapter(this@DailySummaryActivity,viewModel)
                contentDailySummary.summeryRecycler.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@DailySummaryActivity)
                    adapter = dailyAdapter
                }
                gbSummAdapter = GbSummAdapter(this@DailySummaryActivity,viewModel)
                contentDailySummary.gbSummeryRecycler.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@DailySummaryActivity)
                    adapter = gbSummAdapter
                }
                var dateApiText = Util.get_yyyy_mm_dd(Calendar.getInstance())
                callDailySummService(dateApiText?:"")
            }

        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callDailySummService(summeryDate:String){
        try{
            if(!Validation.isOnline(this)){
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
                return
            }else{

                var dailySumRequest=DailySumRequest(summarydate = summeryDate)
                dailySumRequest.deviceversion = Util.getVersionName(this)
                dailySumRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                 println("request date>>>>>> ${Util.getStringFromBean(dailySumRequest)}")
                viewModel.getDailySumm(dailySumRequest)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message?:"")
        }
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showSnackBar(binding.progressBar,it) }
        })

        viewModel.dailySummResponse.observe(this, Observer<DailySummResponse> {
            it?.let {
                println("request date>>>>>> ${Util.getStringFromBean(it)}")
                if(it.status == Constant.STATUS.FAIL){
                    showToast(it.result!!)
                }else{
                    dailyAdapter.notifyDataSetChanged()
                    gbSummAdapter.notifyDataSetChanged()
                    it?.let { setData(it) }
                }
            }
        })


    }

    private fun setData(it: DailySummResponse) {
        try{
            binding.contentDailySummary.apply {
                orderText.text = "${it.data?.orders}"
                prepaidText.text = "${it.data?.prepaid}"
                cashText.text = "${it.data?.cash}"
                gbHeaderText.text = "${it.data?.gbDelivery?.heading?:""}"
            }

        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun createViewModel(): AddTipsViewModel =
        ViewModelProviders.of(this).get(AddTipsViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

    fun openDateDialog(view:View){
        try{
            val calendar = Calendar.getInstance()
            MaterialDialog(this).show {
                datePicker(null,calendar,calendar) { _, date ->
                    date.set(date.get(Calendar.YEAR),date.get(Calendar.MONTH),date.get(Calendar.DATE),0,0,0)
                    var dateText = Util.getMM_day_yyyy(date.time)
                    var dateApiText = Util.get_yyyy_mm_dd(date)
                    //Util.getSelectedDate(date)?.let { fragmentBaseActivity.showToast(it) }
                    binding.contentDailySummary.dateButton.text = dateText
                     callDailySummService(dateApiText?:"")
                }

            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    override fun onResume() {
        super.onResume()


    }

}
