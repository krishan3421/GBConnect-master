package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.discount.adddiscount.AddDiscountRequest
import com.gb.restaurant.model.discount.adddiscount.AddDiscountResponse
import com.gb.restaurant.model.discount.editdiscount.EditDiscountRequest
import com.gb.restaurant.model.discount.editdiscount.EditDiscountResponse
import com.gb.restaurant.model.discount.getdiscount.Discount
import com.gb.restaurant.model.discount.getdiscount.DiscountRequest
import com.gb.restaurant.model.discount.getdiscount.GetDiscountResponse
import com.gb.restaurant.model.discount.rmdiscount.RmDiscountRequest
import com.gb.restaurant.model.discount.rmdiscount.RmDiscountResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.MarketingAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.CouponViewModel
import kotlinx.android.synthetic.main.activity_market_coupon.*
import kotlinx.android.synthetic.main.add_offer_layout.*
import kotlinx.android.synthetic.main.content_market_coupon.*
import kotlinx.android.synthetic.main.custom_appbar.*

class MarketCouponActivity : BaseActivity() ,View.OnClickListener{
    companion object{
        val TAG:String = MarketCouponActivity::class.java.simpleName
    }
    private lateinit var viewModel: CouponViewModel
    private lateinit var marketingAdapter: MarketingAdapter
    var rsLoginResponse: RsLoginResponse? = null
    var dialog:MaterialDialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        setContentView(R.layout.activity_market_coupon)
        initData()
        initView()
    }

    private fun statusBarTransparent(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
    fun backClick(v: View){
        onBackPressed()
    }


    private fun initData(){
        try{

            rsLoginResponse = MyApp.instance.rsLoginResponse
            viewModel = createViewModel()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }
    private fun initView(){
        try{
            create_offer_button.setOnClickListener(this)
            attachObserver()
            back_layout.setOnClickListener {
                onBackPressed()
            }
            marketingAdapter = MarketingAdapter(this,viewModel)
            offer_recyclerview.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@MarketCouponActivity)
                adapter = marketingAdapter
            }
            adapterSize()
            callGetDiscountService()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()
        try{
            marketingAdapter.setOnItemClickListener(object :MarketingAdapter.MarketClickListener{
                override fun onEditItemClick(discount: Discount, position: Int, v: View) {
                    openAddEditOfferPopUp(isEdit = true,discount = discount)

                }

                override fun onResetItemClick(discount: Discount, position: Int, v: View) {
                    removeAlert(discount)
                }

            })
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callGetDiscountService(){
        try{
            if(Validation.isOnline(this)){
                var discountRequest = DiscountRequest()
                discountRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                discountRequest.deviceversion = Util.getVersionName(this)
               // println("activeresponse>>> ${Util.getStringFromBean(discountRequest)}")
                viewModel.getDiscounts(discountRequest)
            }else{
                showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun removeAlert(discount: Discount) {
        MaterialDialog(this).show {
            title(R.string.app_name_alert)
            message(R.string.remove_discount_msg)
            positiveButton {
                callrmDiscountService(discount.id!!)
            }
            negativeButton(R.string.cancel)
            positiveButton(R.string.ok)
        }
    }
    private fun callrmDiscountService(discountId:String){
        try{
            if(Validation.isOnline(this)){
                var rmDiscountRequest=RmDiscountRequest()
                rmDiscountRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                rmDiscountRequest.deviceversion = Util.getVersionName(this)
                rmDiscountRequest.discountid = discountId
                // println("activeresponse>>> ${Util.getStringFromBean(discountRequest)}")
                viewModel.rmDiscount(rmDiscountRequest)
            }else{
                showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun calladdDiscountService(addDiscountRequest:AddDiscountRequest){
        try{
            if(Validation.isOnline(this)){
                addDiscountRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                addDiscountRequest.deviceversion = Util.getVersionName(this)
                // println("activeresponse>>> ${Util.getStringFromBean(discountRequest)}")
                viewModel.addDiscount(addDiscountRequest)
            }else{
                dialog?.offer_progress?.visibility=View.GONE
                showSnackBar(progress_bar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun callEditDiscountService(editDiscountRequest: EditDiscountRequest){
        try{
            if(Validation.isOnline(this)){
                editDiscountRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                editDiscountRequest.deviceversion = Util.getVersionName(this)
                 println("editRequest>>> ${Util.getStringFromBean(editDiscountRequest)}")
                viewModel.editDiscount(editDiscountRequest)
            }else{
                dialog?.offer_progress?.visibility=View.GONE
                showSnackBar(progress_bar,getString(R.string.internet_connected))
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
            dialog?.offer_progress?.visibility=View.GONE
            it?.let { showSnackBar(progress_bar,it) }
        })

        viewModel.getDiscountResponse.observe(this, Observer<GetDiscountResponse> {
            it?.let {
                if(it.status !=Constant.STATUS.FAIL){
                    user_size_text.text = "${it.users}"
                    month_text.text = "${it.month}"
                    showToast(it.result?:"")
                }
                marketingAdapter.notifyDataSetChanged()

                adapterSize()
            }
        })
        viewModel.rmDiscountResponse.observe(this, Observer<RmDiscountResponse> {
            it?.let {
                if(it.status !=Constant.STATUS.FAIL){
                   callGetDiscountService()
                    openAddEditOfferPopUp(isEdit = false)
                }else{
                    Util.alert("${it.result?:""}",this)
                }

            }
        })

        viewModel.addDiscountResponse.observe(this, Observer<AddDiscountResponse> {
            it?.let {
                dialog?.offer_progress?.visibility=View.GONE
                if(it.status ==Constant.STATUS.FAIL){
                    // Util.alert("No offer list",this)
                }else{
                    dialog?.dismiss()
                    callGetDiscountService()
                }
                Util.alert("${it.result?:""}",this)

            }
        })
        viewModel.editDiscountResponse.observe(this, Observer<EditDiscountResponse> {
            it?.let {
                dialog?.offer_progress?.visibility=View.GONE
                if(it.status !=Constant.STATUS.FAIL){
                    dialog?.dismiss()
                    callGetDiscountService()
                }
                Util.alert("${it.result?:""}",this)

            }
        })


    }

    private fun adapterSize(){
        if(marketingAdapter.itemCount <4){
            create_offer_button.visibility=View.VISIBLE
        }else{
            create_offer_button.visibility=View.GONE
        }
        if(marketingAdapter.itemCount <1){
            no_offer_text.visibility=View.VISIBLE
            offer_recyclerview.visibility=View.GONE
        }else{
            no_offer_text.visibility=View.GONE
            offer_recyclerview.visibility=View.VISIBLE
        }
    }


    private fun createViewModel(): CouponViewModel =
        ViewModelProviders.of(this).get(CouponViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

    override fun onClick(view: View?) {
        when(view){
            create_offer_button->{
                openAddEditOfferPopUp(isEdit = false)
            }
        }
    }
    private fun openAddEditOfferPopUp(isEdit:Boolean=false,discount: Discount?=null){
         var status = "$"
        try {
            val dialog = MaterialDialog(this).show {
                this.cancelOnTouchOutside(false)
                cornerRadius(null,R.dimen.dimen_30)
                customView(R.layout.add_offer_layout, scrollable = false, noVerticalPadding = true, horizontalPadding = false)
                title_dialog_text.text = "Enter Offer Detail"
                continue_button.text="Save"
                if(isEdit){
                    spend_text.text = "Spend Minimum $"
                    get_text.text = "Get ${discount?.types} off"
                    min_edittext.setText("${discount?.minorder}")
                    off_edittext.setText("${discount?.details}")
                  //  continue_button.text="Edit"
                    if(discount?.types=="$"){
                        doller_radio.isChecked =true
                    }else{
                        percent_radio.isChecked =true
                    }
                }else{
                   // continue_button.text="Add"
                }
                close_dialog.setOnClickListener {
                    this.dismiss()
                }
                dialog =this
                offer_radio_group.setOnCheckedChangeListener { radioGroup, checkedId ->
                    val radio: RadioButton = findViewById(checkedId)
                    when(radio){
                        doller_radio->{
                            status ="$"
                            get_text.text = "Get $status off"
                        }
                        percent_radio->{
                            status ="%"
                            get_text.text = "Get $status off"
                        }
                    }
                }
                continue_button.setOnClickListener {
                       offer_progress.visibility=View.VISIBLE
                        if(isEdit){
                            initAddDiscount(min_edittext.text.toString(),status,off_edittext.text.toString(),discount?.id!!,isEdit=true)
                        }else{
                            initAddDiscount(min_edittext.text.toString(),status,off_edittext.text.toString())
                        }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initAddDiscount(minamount:String,discounttype:String,discounts:String, discountId:String="",isEdit:Boolean=false){
        try{

            if(minamount.isNullOrEmpty()){
                Util.alert("Minimum amount should not empty.",this)
                dialog?.offer_progress?.visibility=View.GONE
                return
            }
            if(discounttype.isNullOrEmpty()){
                Util.alert("Discount Type should not empty.",this)
                dialog?.offer_progress?.visibility=View.GONE
                return
            }
            if(discounts.isNullOrEmpty()){
                Util.alert("Discount should not empty.",this)
                dialog?.offer_progress?.visibility=View.GONE
                return
            }
            if(isEdit){
                var editDiscountRequest = EditDiscountRequest()
                editDiscountRequest.minamount = minamount
                editDiscountRequest.discounttype = discounttype
                editDiscountRequest.discounts = discounts
                editDiscountRequest.discountid=discountId
                callEditDiscountService(editDiscountRequest)
            }else {
                var addDiscountRequest=AddDiscountRequest()
                addDiscountRequest.minamount = minamount
                addDiscountRequest.discounttype = discounttype
                addDiscountRequest.discounts = discounts
                calladdDiscountService(addDiscountRequest)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

}
