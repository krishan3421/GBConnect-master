package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.ActivityMarketCouponBinding
import com.gb.restaurant.databinding.HomeDetailActivityBinding
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

class MarketCouponActivity : BaseActivity() ,View.OnClickListener{
    companion object{
        val TAG:String = MarketCouponActivity::class.java.simpleName
    }
    private lateinit var viewModel: CouponViewModel
    private lateinit var marketingAdapter: MarketingAdapter
    var rsLoginResponse: RsLoginResponse? = null
    var dialog:MaterialDialog?=null
    private lateinit var binding: ActivityMarketCouponBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
       // setContentView(R.layout.activity_market_coupon)
        binding = ActivityMarketCouponBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            binding.contentMarketCoupon.createOfferButton.setOnClickListener(this)
            attachObserver()
            binding.customAppbar.backLayout.setOnClickListener {
                onBackPressed()
            }
            marketingAdapter = MarketingAdapter(this,viewModel)
            binding.contentMarketCoupon.offerRecyclerview.apply {
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
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
                val offerProgress = dialog?.findViewById<ProgressBar>(R.id.offer_progress)
                offerProgress?.visibility=View.GONE
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
                val offerProgress = dialog?.findViewById<ProgressBar>(R.id.offer_progress)
                offerProgress?.visibility=View.GONE
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }


    private fun attachObserver() {
        val offerProgress = dialog?.findViewById<ProgressBar>(R.id.offer_progress)
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            offerProgress?.visibility=View.GONE
            it?.let { showSnackBar(binding.progressBar,it) }
        })

        viewModel.getDiscountResponse.observe(this, Observer<GetDiscountResponse> {
            it?.let {
                if(it.status != Constant.STATUS.FAIL){
                    binding.contentMarketCoupon.userSizeText.text = "${it.users}"
                    binding.contentMarketCoupon.monthText.text = "${it.month}"
                    showToast(it.result?:"")
                }
                marketingAdapter.notifyDataSetChanged()

                adapterSize()
            }
        })
        viewModel.rmDiscountResponse.observe(this, Observer<RmDiscountResponse> {
            it?.let {
                if(it.status != Constant.STATUS.FAIL){
                   callGetDiscountService()
                    openAddEditOfferPopUp(isEdit = false)
                }else{
                    Util.alert("${it.result?:""}",this)
                }

            }
        })

        viewModel.addDiscountResponse.observe(this, Observer<AddDiscountResponse> {
            it?.let {
                offerProgress?.visibility=View.GONE
                if(it.status == Constant.STATUS.FAIL){
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
                offerProgress?.visibility=View.GONE
                if(it.status != Constant.STATUS.FAIL){
                    dialog?.dismiss()
                    callGetDiscountService()
                }
                Util.alert("${it.result?:""}",this)

            }
        })


    }

    private fun adapterSize(){
        binding.contentMarketCoupon.apply {
            if(marketingAdapter.itemCount <4){
                createOfferButton.visibility=View.VISIBLE
            }else{
                createOfferButton.visibility=View.GONE
            }
            if(marketingAdapter.itemCount <1){
                noOfferText.visibility=View.VISIBLE
                offerRecyclerview.visibility=View.GONE
            }else{
                noOfferText.visibility=View.GONE
                offerRecyclerview.visibility=View.VISIBLE
            }
        }

    }


    private fun createViewModel(): CouponViewModel =
        ViewModelProviders.of(this).get(CouponViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) binding.progressBar.visibility = View.VISIBLE else  binding.progressBar.visibility = View.GONE
    }

    override fun onClick(view: View?) {
        when(view){
            binding.contentMarketCoupon.createOfferButton->{
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
                val titleDialogText = this.findViewById<TextView>(R.id.title_dialog_text)
                val continueButton = this.findViewById<Button>(R.id.continue_button)
                val spendText = this.findViewById<TextView>(R.id.spend_text)
                val getText = this.findViewById<TextView>(R.id.get_text)
                val minEditText = this.findViewById<EditText>(R.id.min_edittext)
                val offEditText = this.findViewById<EditText>(R.id.off_edittext)
                val closeDialog = this.findViewById<ImageView>(R.id.close_dialog)
                val dollerRadio = this.findViewById<RadioButton>(R.id.doller_radio)
                val percentRadio = this.findViewById<RadioButton>(R.id.percent_radio)
                val offer_radio_group = this.findViewById<RadioGroup>(R.id.offer_radio_group)
                val offerProgress = this.findViewById<ProgressBar>(R.id.offer_progress)
                titleDialogText.text = "Enter Offer Detail"
                continueButton.text="Save"
                if(isEdit){
                    spendText.text = "Spend Minimum $"
                    getText.text = "Get ${discount?.types} off"
                    minEditText.setText("${discount?.minorder}")
                    offEditText.setText("${discount?.details}")
                  //  continue_button.text="Edit"
                    if(discount?.types=="$"){
                        dollerRadio.isChecked =true
                    }else{
                        percentRadio.isChecked =true
                    }
                }else{
                   // continue_button.text="Add"
                }
                closeDialog.setOnClickListener {
                    this.dismiss()
                }
                dialog =this
                offer_radio_group.setOnCheckedChangeListener { radioGroup, checkedId ->
                    val radio: RadioButton = findViewById(checkedId)
                    when(radio){
                        dollerRadio->{
                            status ="$"
                            getText.text = "Get $status off"
                        }
                        percentRadio->{
                            status ="%"
                            getText.text = "Get $status off"
                        }
                    }
                }
                continueButton.setOnClickListener {
                    offerProgress.visibility=View.VISIBLE
                        if(isEdit){
                            initAddDiscount(minEditText.text.toString(),status,offEditText.text.toString(),discount?.id!!,isEdit=true)
                        }else{
                            initAddDiscount(minEditText.text.toString(),status,offEditText.text.toString())
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
          val offerProgress = dialog?.findViewById<ProgressBar>(R.id.offer_progress)
            if(minamount.isNullOrEmpty()){
                Util.alert("Minimum amount should not empty.",this)
                offerProgress?.visibility=View.GONE
                return
            }
            if(discounttype.isNullOrEmpty()){
                Util.alert("Discount Type should not empty.",this)
                offerProgress?.visibility=View.GONE
                return
            }
            if(discounts.isNullOrEmpty()){
                Util.alert("Discount should not empty.",this)
                offerProgress?.visibility=View.GONE
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
