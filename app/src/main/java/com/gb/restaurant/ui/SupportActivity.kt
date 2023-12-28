package com.gb.restaurant.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.DialogBehavior
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.ModalDialog
import com.afollestad.materialdialogs.customview.customView
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.ActivitySupportBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.enumm.Support
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.support.SupportItem
import com.gb.restaurant.model.support.SupportRequest
import com.gb.restaurant.model.support.SupportResponse
import com.gb.restaurant.session.SessionManager
import com.gb.restaurant.ui.adapter.SupportAdapter
import com.gb.restaurant.utils.ListPaddingDecorationGray
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.SupportViewModel

class SupportActivity : BaseActivity() {

    private lateinit var supportAdapter: SupportAdapter
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: SupportViewModel
    private var list:MutableList<SupportItem> = ArrayList()
    private lateinit var binding: ActivitySupportBinding
    lateinit var sessionManager: SessionManager
    companion object{
        val TAG:String = SupportActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_support)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()

    }

    private fun initData(){
        try{
             sessionManager= SessionManager(applicationContext)
            rsLoginResponse = MyApp.instance.rsLoginResponse
            viewModel = createViewModel()
            val support0= SupportItem("Request a call Back", Support.REQUEST_CALL_BACK)
            val support1= SupportItem("Help With adding Tips",Support.HELP_ADDING_TIPS)
            val support2= SupportItem("Help with adding extra Item in Order",Support.HELP_ADDING_ITEM)
            val support3= SupportItem("Help Cancel or Refund Order to customer",Support.HELP_CANCEL_REFUND)
            val support4= SupportItem("Update of Menu",Support.UPDATE_MENU)
            val support5= SupportItem("Add New store with Grabull",Support.ADDING_NEW_STORE)
            val support6= SupportItem("Free website Upgrade and Marketing Help",Support.FREE_WEBSITE)
            val support7= SupportItem("Reviews on Google (Reputation management)",Support.REVIEW_ON_GOOGLE)
            val support8= SupportItem("Marketing Packages",Support.MARKETING_PACKAGE)
            val support9= SupportItem("Payment & Bank Account Query",Support.PAYMENT_BANK)
            println("apiType>>>>>> ${sessionManager.getApiType()}")
            if(sessionManager.getApiType()===Constant.API_TYPE.GB) {
                println("apiType>>>>>> ${sessionManager.getApiType()}")
                list.add(support0)
            }
            list.add(support1)
            list.add(support2)
            list.add(support3)
            list.add(support4)
            list.add(support5)
            list.add(support6)
            list.add(support7)
            list.add(support8)
            list.add(support9)

        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    private fun initView(){
        try{
            setSupportActionBar(binding.toolbar)
            binding.toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
            supportAdapter = SupportAdapter(this,list)
            binding.contentSupport.supportRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@SupportActivity)
                adapter = supportAdapter
                addItemDecoration(ListPaddingDecorationGray(this@SupportActivity, 0,0))
            }

            attachObserver()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()
        supportAdapter.setOnItemClickListener(object :SupportAdapter.SupportClickListener{
            override fun onItemClick(supportItem: SupportItem,position:Int, v: View) {
//                if(supportItem.index==Support.REQUEST_CALL_BACK){
//                    showCustomViewDialog()
//                }else
              if(supportItem.index==Support.HELP_ADDING_TIPS){
                    goToPage(AddTipsActivity::class.java)
                }else if(supportItem.index==Support.HELP_ADDING_ITEM){
                    goToPage(AddItemActivity::class.java)
                }else if(supportItem.index==Support.HELP_CANCEL_REFUND){
                    goToPage(CancelRefundActivity::class.java)
                }else if(supportItem.index==Support.UPDATE_MENU){//Update of Menu
                    Util.alert("Comming soon",this@SupportActivity)
                }else if(supportItem.index==Support.ADDING_NEW_STORE){//Update of Menu
                  openURL("https://www.grabullmarketing.com/new-restaurant-sign-up/")
                }else if(supportItem.index==Support.FREE_WEBSITE){//Update of Menu
                    openURL("https://www.grabullmarketing.com/free-website-for-restaurants/")
                }else if(supportItem.index==Support.REVIEW_ON_GOOGLE){//Update of Menu
                    Util.alert("Comming soon",this@SupportActivity)
                }else if(supportItem.index==Support.MARKETING_PACKAGE){//Update of Menu
                    openURL("https://www.grabullmarketing.com/restaurant-marketing-services/")
                }else if(supportItem.index==Support.PAYMENT_BANK){//Update of Menu
                    paymentQueryDialog()
                }

            }

        } )
    }

    private fun goToPage(cls: Class<*>?){
        try{
            var intent = Intent(this@SupportActivity,cls)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    private fun openURL(url:String){
        var intent = Intent(this, ViewInvoiceActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(ViewInvoiceActivity.INVOICE, "$url")
        startActivity(intent)
    }

    private fun paymentQueryDialog(dialogBehavior: DialogBehavior = ModalDialog) {

        val dialog = MaterialDialog(this, dialogBehavior).show {
            this.setCanceledOnTouchOutside(false)
            cornerRadius(null,R.dimen.dimen_30)
            customView(R.layout.payment_query_dialog, scrollable = false,noVerticalPadding = true, horizontalPadding = false)
            val cancelImage = this.findViewById<ImageView>(R.id.cancel_image)
            val submitButton = this.findViewById<Button>(R.id.submit_button_payment)
            val detailEditPayment = this.findViewById<EditText>(R.id.detail_edit_payment)
            var supportRequest = SupportRequest()
            supportRequest.deviceversion = Util.getVersionName(this@SupportActivity)
            supportRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
            cancelImage.setOnClickListener {
                this.dismiss()
            }

            submitButton.setOnClickListener {
                if(detailEditPayment.text.isNullOrEmpty()){
                    showToast("Please enter your payment query.")
                }else{
                    supportRequest.querytype="Payment"
                    supportRequest.details = detailEditPayment.text.toString()?:""
                    callService(supportRequest)
                    this.dismiss()
                }
            }
        }
    }

    private fun showCustomViewDialog(dialogBehavior: DialogBehavior = ModalDialog) {

        val dialog = MaterialDialog(this, dialogBehavior).show {
            this.setCanceledOnTouchOutside(false)
            cornerRadius(null,R.dimen.dimen_30)
            customView(R.layout.support_dialog, scrollable = false,noVerticalPadding = true, horizontalPadding = false)
            val callBackImage = this.findViewById<Button>(R.id.call_back_image)
            val closeImage =this.findViewById<ImageView>(R.id.close_image)
            val submitButton = this.findViewById<Button>(R.id.submit_button)
            val detailEditText = this.findViewById<EditText>(R.id.detail_edit)
            var supportRequest = SupportRequest()
            supportRequest.deviceversion = Util.getVersionName(this@SupportActivity)
            supportRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
            closeImage.setOnClickListener {
                this.dismiss()
            }
            callBackImage.setOnClickListener {
                callService(supportRequest)
                this.dismiss()
            }
            submitButton.setOnClickListener {
                if(detailEditText.text.isNullOrEmpty()){
                    showToast("Detail box is empty.")
                }else{
                    supportRequest.details = detailEditText.text.toString()
                    callService(supportRequest)
                    this.dismiss()
                }
            }
        }
    }

    private fun callService(supportRequest : SupportRequest){
        try{
            if(Validation.isOnline(this)){
                viewModel.getCallBack(supportRequest)
            }else{
               showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
            it?.let { showSnackBar(binding.progressBar,it) }
        })
        viewModel.supportResponse.observe(this, Observer<SupportResponse> {
            it?.let {
                if(it.status == Constant.STATUS.FAIL){
                    showToast(it.result!!)
                }else{
                    showToast(it.result!!)
                }
            }
        })


    }

    private fun createViewModel(): SupportViewModel =
        ViewModelProviders.of(this).get(SupportViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

}
