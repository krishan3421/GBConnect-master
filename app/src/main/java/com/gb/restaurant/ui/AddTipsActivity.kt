package com.gb.restaurant.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.addordertips.OrderTipsRequest
import com.gb.restaurant.model.addordertips.OrderTipsResponse
import com.gb.restaurant.model.addtips.ActiveOrderRequest
import com.gb.restaurant.model.addtips.ActiveOrderResponse
import com.gb.restaurant.model.addtips.OrderSearchRequest
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.AddingTipsAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.AddTipsViewModel

import kotlinx.android.synthetic.main.activity_add_tips.*
import kotlinx.android.synthetic.main.card_detail_layout.*
import kotlinx.android.synthetic.main.content_add_tips.*
import kotlinx.android.synthetic.main.content_add_tips.no_order_text
import kotlinx.android.synthetic.main.content_add_tips.search_edittext
import kotlinx.android.synthetic.main.content_item.*
import kotlinx.android.synthetic.main.custom_appbar.*

class AddTipsActivity : BaseActivity() {

    companion object{
        val TAG = AddTipsActivity::class.java.simpleName
    }
    private lateinit var addingTipsAdapter: AddingTipsAdapter
    private var list:MutableList<String> = ArrayList()
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: AddTipsViewModel
    var selOrderTipsRequest : OrderTipsRequest?=null
    private var cardDialog:MaterialDialog?=null
    private var fromSearch:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        setContentView(R.layout.activity_add_tips)
        initData()
        initView()

    }

    private fun initData() {
        try{
            rsLoginResponse = MyApp.instance.rsLoginResponse
            for(i in 0..20){
                list.add("$i")
            }
            viewModel = createViewModel()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
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

    private fun initView(){
        try{
            back_layout.setOnClickListener {
                onBackPressed()
            }
            attachObserver()

            addingTipsAdapter = AddingTipsAdapter(this,viewModel)
            adding_tips_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@AddTipsActivity)
                adapter = addingTipsAdapter
            }
            callService()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callService(){
        try{
            if(Validation.isOnline(this)){
                var activeOrderRequest = ActiveOrderRequest()
                activeOrderRequest.deviceversion = Util.getVersionName(this)
                activeOrderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
               // println("request date>>>>>> ${Util.getStringFromBean(reportRequest)}")
                fromSearch=false
                viewModel.getActiveOrderList(activeOrderRequest)
            }else{
                showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
 fun searchTips(v:View){
     try{
         callSearchService()
     }catch (e:Exception){
         e.printStackTrace()
         Log.e(TAG,e.message!!)
     }
 }
    private fun callSearchService(){
        try{
            if(!Validation.isOnline(this)){
                showSnackBar(progress_bar,getString(R.string.internet_connected))
                return
            }else if(search_edittext.text.toString().isNullOrEmpty()){
                showSnackBar(progress_bar,"Please provide valid item Id")
                return
            }else{
                var orderSearchRequest = OrderSearchRequest()
                orderSearchRequest.deviceversion = Util.getVersionName(this)
                orderSearchRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderSearchRequest.order_id=search_edittext.text.toString()
                 println("request date>>>>>> ${Util.getStringFromBean(orderSearchRequest)}")
                fromSearch=true
                viewModel.getOrderSearch(orderSearchRequest)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()
        addingTipsAdapter.setOnItemClickListener(object :AddingTipsAdapter.AddingTipsListener{
            override fun onItemClick(addTipsdata: com.gb.restaurant.model.addtips.Data?, v: View) {
                showCustomViewDialog(addTipsdata)
            }

        } )
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showSnackBar(progress_bar,it) }
        })
        viewModel.response.observe(this, Observer<ActiveOrderResponse> {
            it?.let {
                addingTipsAdapter.notifyDataSetChanged()
                if(addingTipsAdapter.itemCount >0){
                    adding_tips_recycler.visibility = View.VISIBLE
                    no_order_text.visibility = View.GONE}
                else{
                    adding_tips_recycler.visibility = View.GONE
                    no_order_text.visibility = View.VISIBLE
                    if(fromSearch){
                        no_order_text.text=getString(R.string.oops_no_order_found_with_order_id_tips)
                    }else{
                        no_order_text.text=getString(R.string.oops_no_order_found)
                    }
                }
            }
        })
        viewModel.addTipsResponse.observe(this, Observer<OrderTipsResponse> {
            it?.let {
                if(it.status == Constant.STATUS.FAIL){
                    Util.alertDialog(it.result?:"",this)
                    cardDialog?.card_progress?.visibility =View.GONE
                }else{
                    if(it.result.equals("Tips charged successfully",true) || it.result.equals("Tips added successfully",true)){
                        cardDialog?.dismiss()
                        Util.alertDialog(it.result?:"",this)
                    }else{
                        cardDialog?.card_progress?.visibility =View.GONE
                        selOrderTipsRequest?.newcard = "Yes"
                        if(cardDialog ==null) {
                            showCardDetailDialog()
                        }else{
                            if(!cardDialog!!.isShowing){
                                showCardDetailDialog()
                            }
                        }
                        if(it.data?.payment.equals("Failed"))
                            Util.alertDialog(it.data?.result?:"",this)
                    }
                }
            }
        })


    }
    private fun showCustomViewDialog(addTipsData: com.gb.restaurant.model.addtips.Data?, dialogBehavior: DialogBehavior = ModalDialog) {
        var orderTipsRequest = OrderTipsRequest()
        var dialog = Dialog(this, R.style.AppCompatAlertDialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.tips_layout)
        dialog.setCanceledOnTouchOutside(true)
        val titleHeader = dialog.findViewById<View>(R.id.title_header_text) as TextView
        val cancelImage = dialog.findViewById<View>(R.id.cancel_image) as ImageView
        val addTipsButton = dialog.findViewById<View>(R.id.add_tips_button) as TextView
        val tipsEdit = dialog.findViewById<View>(R.id.tips_edit) as EditText
        titleHeader.text = "${addTipsData?.id}"
        cancelImage.setOnClickListener {
            dialog.dismiss()
        }
        addTipsButton.setOnClickListener {
            if(!tipsEdit.text.isNullOrEmpty()){
                orderTipsRequest.order_id= addTipsData?.id!!
                orderTipsRequest.order_tips = tipsEdit.text.toString()
                orderTipsRequest.newcard = "No"
                selOrderTipsRequest = orderTipsRequest
                callTipsService(orderTipsRequest)
                dialog.dismiss()
            }else{
                showToast("Please add Tips")
            }
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private fun showCardDetailDialog() {
        // dialogBehavior: DialogBehavior = ModalDialog
        val dialog = MaterialDialog(this).show {
            this.cancelOnTouchOutside(false)
            cornerRadius(null,R.dimen.dimen_30)
            customView(R.layout.card_detail_layout, scrollable = false, noVerticalPadding = true, horizontalPadding = false)
            title_dialog_text.text = "Enter Card Detail"
            close_dialog.setOnClickListener {
                this.dismiss()
            }
            continue_button.setOnClickListener {
                if(card_edittext.text.toString().isNullOrEmpty() || card_edittext.text.length <15){
                    Util.alertDialog("Please add Valid Card Number",this@AddTipsActivity)
                    return@setOnClickListener
                }
                if(exp_edittext.text.toString().isNullOrEmpty() || exp_edittext.text.length <4){
                    Util.alertDialog("Please add Valid Expiry Date",this@AddTipsActivity)
                    return@setOnClickListener
                }
                if(cvv_edittext.text.toString().isNullOrEmpty() || cvv_edittext.text.length <3){
                    Util.alertDialog("Please add Valid CVV",this@AddTipsActivity)
                    return@setOnClickListener
                }
                if(zip_edittext.text.toString().isNullOrEmpty() || zip_edittext.text.length <5){
                    Util.alertDialog("Please add Valid Zip Code",this@AddTipsActivity)
                    return@setOnClickListener
                }
                if(card_holder_edittext.text.toString().isNullOrEmpty()){
                    Util.alertDialog("Please add card holder name",this@AddTipsActivity)
                    return@setOnClickListener
                }
                card_progress.visibility = View.VISIBLE
                cardDialog = this
                selOrderTipsRequest?.card = card_edittext.text.toString()
                selOrderTipsRequest?.expiry = exp_edittext.text.toString()
                selOrderTipsRequest?.cvv = cvv_edittext.text.toString()
                selOrderTipsRequest?.billingzip = zip_edittext.text.toString()
                selOrderTipsRequest?.cardholder = card_holder_edittext.text.toString()
                callTipsService(selOrderTipsRequest!!)
            }
        }

    }

    private fun callTipsService(orderTipsRequest: OrderTipsRequest){
        try{
            if(Validation.isOnline(this)){
                orderTipsRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderTipsRequest.deviceversion = Util.getVersionName(this)
                println("tips request>>>>>>> ${Util.getStringFromBean(orderTipsRequest)}")
                viewModel.addTips(orderTipsRequest)
            }else{
                showToast(getString(R.string.internet_connected))
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
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

}
