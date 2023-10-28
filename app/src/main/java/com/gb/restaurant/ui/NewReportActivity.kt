package com.gb.restaurant.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gb.restaurant.databinding.ActivityNewReportBinding
import com.gb.restaurant.enumm.ReportsDetail
import com.gb.restaurant.ui.adapter.NewReportAdapter

class NewReportActivity : BaseActivity() {

    private lateinit var newReportAdapter: NewReportAdapter
    private var list:MutableList<String> = ArrayList()
    private lateinit var binding: ActivityNewReportBinding
    companion object{
        private val TAG = NewReportActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        //setContentView(R.layout.activity_new_report)
        binding = ActivityNewReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }

    private fun statusBarTransparent(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
      /*  if (Build.VERSION.SDK_INT in 19..20) {
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
            list.add("Daily Summary")
            list.add("Monthly Meal Tax & Other Charges")
            list.add("Monthly & Weekly Invoices")
            list.add("Orders by Weekly")
            list.add("How to Grow More Sales ?")
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initView(){
        try{
            binding.customAppbar.backLayout.setOnClickListener {
                onBackPressed()
            }
            newReportAdapter = NewReportAdapter(this,list)
            binding.contentNewReport.newReportRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@NewReportActivity)
                adapter = newReportAdapter
                // addItemDecoration(ListPaddingDecorationGray(this@SupportActivity, 0,0))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()

        newReportAdapter.setOnItemClickListener(object :NewReportAdapter.ReportClickListener{
            override fun onItemClick(position:Int, v: View) {

                when(position){
                    0->{
                      //  Util.alert("Coming soon",this@NewReportActivity)
                        openOrderSummaryPage()
                    }
                    1->{
                        //Util.alert("Coming soon",this@NewReportActivity)
                        openDetailPage(ReportsDetail.MONTHLY_MEALS.ordinal)
                    }
                    2->{
                        openDetailPage(ReportsDetail.MONTHLY_INVOICE.ordinal)
                    }
                    3->{
                        openDetailPage(ReportsDetail.OTHERS.ordinal)
                    }
                    4->{
                        //https://www.grabullmarketing.com/restaurant-seo-marketing-price/
                        //https://www.grabullmarketing.com/restaurant-marketing-services/
                       openUrlPage("https://www.grabullmarketing.com/restaurant-seo-marketing-price/")
                    }else->{
                    openDetailPage(ReportsDetail.MONTHLY_MEALS.ordinal)
                }
                }

            }

        } )
    }

    private fun openDetailPage( type:Int){
        var intent = Intent(this,ReportsDetailActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(ReportsDetailActivity.FRAGMENT_KEY,type)
        startActivity(intent)
    }
    private fun openUrlPage(url:String){
        var intent = Intent(this, ViewInvoiceActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(ViewInvoiceActivity.INVOICE, url)
        startActivity(intent)
    }

    private fun openOrderSummaryPage(){
        var intent = Intent(this,DailySummaryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun backClick(v: View){
        onBackPressed()
    }

}
