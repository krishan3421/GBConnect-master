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
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.daily.DailySumRequest
import com.gb.restaurant.model.daily.DailySummResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.DailySummAdapter
import com.gb.restaurant.ui.adapter.GbSummAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.AddTipsViewModel
import kotlinx.android.synthetic.main.activity_daily_summary.*
import kotlinx.android.synthetic.main.content_daily_summary.*
import kotlinx.android.synthetic.main.custom_appbar.*
import java.util.*


class DailySummaryActivity : BaseActivity() {

    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: AddTipsViewModel
    private lateinit var dailyAdapter: DailySummAdapter
    private lateinit var gbSummAdapter: GbSummAdapter
    companion object{
        private val TAG = DailySummaryActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        setContentView(R.layout.activity_daily_summary)
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
            back_layout.setOnClickListener {
                onBackPressed()
            }
            date_button.text = "${Util.getMM_day_yyyy(Date())}"
            attachObserver()
            dailyAdapter = DailySummAdapter(this,viewModel)
            summery_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@DailySummaryActivity)
                adapter = dailyAdapter
            }
            gbSummAdapter = GbSummAdapter(this,viewModel)
            gb_summery_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@DailySummaryActivity)
                adapter = gbSummAdapter
            }
            callDailySummService()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callDailySummService(){
        try{
            if(!Validation.isOnline(this)){
                showSnackBar(progress_bar,getString(R.string.internet_connected))
                return
            }else{

                var dailySumRequest=DailySumRequest()
                dailySumRequest.deviceversion = Util.getVersionName(this)
                dailySumRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                 println("request date>>>>>> ${Util.getStringFromBean(dailySumRequest)}")
                viewModel.getDailySumm(dailySumRequest)
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
            it?.let { showSnackBar(progress_bar,it) }
        })

        viewModel.dailySummResponse.observe(this, Observer<DailySummResponse> {
            it?.let {
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
            order_text.text = "${it.data?.orders}"
            prepaid_text.text = "${it.data?.prepaid}"
            cash_text.text = "${it.data?.cash}"
            gb_header_text.text = "${it.data?.gbDelivery?.heading?:""}"
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
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

    fun openDateDialog(view:View){
        try{
            var calendar = Calendar.getInstance()
            MaterialDialog(this).show {
                datePicker(null,calendar,calendar) { _, date ->
                    date.set(date.get(Calendar.YEAR),date.get(Calendar.MONTH),date.get(Calendar.DATE),0,0,0)
                    var dateText = Util.getMM_day_yyyy(date.time)
                    //Util.getSelectedDate(date)?.let { fragmentBaseActivity.showToast(it) }
                    this@DailySummaryActivity.date_button.text = dateText

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
