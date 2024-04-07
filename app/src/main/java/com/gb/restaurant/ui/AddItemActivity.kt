package com.gb.restaurant.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.ActivityAddItemBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.additem.AddOrderItemRequest
import com.gb.restaurant.model.additem.AddOrderItemResponse
import com.gb.restaurant.model.addtips.ActiveOrderRequest
import com.gb.restaurant.model.addtips.ActiveOrderResponse
import com.gb.restaurant.model.addtips.OrderSearchRequest
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.AddingItemAdapter
import com.gb.restaurant.ui.adapter.ExtraItemAdapter
import com.gb.restaurant.ui.adapter.ItemAdapter
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.AddTipsViewModel


class AddItemActivity : BaseActivity() {

    companion object{
        val TAG = AddItemActivity::class.java.simpleName
    }
    private lateinit var addingItemAdapter: AddingItemAdapter
    private var list:MutableList<String> = ArrayList()
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var viewModel: AddTipsViewModel
    var itemData:com.gb.restaurant.model.addtips.Data? = null
    var selOrderItemRequest:AddOrderItemRequest?=null
    private var cardDialog:MaterialDialog?=null
    private var fromSearch:Boolean=false
    private lateinit var binding: ActivityAddItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
       // setContentView(R.layout.activity_add_item)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            Log.e(TAG, e.message!!)
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
            binding.customAppbar.backLayout.setOnClickListener {
                onBackPressed()
            }
            attachObserver()

            addingItemAdapter = AddingItemAdapter(this,viewModel)
            binding.contentItem.addingItemsRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@AddItemActivity)
                adapter = addingItemAdapter
            }
            binding.contentItem.needMoreHelpButton.setOnClickListener {
                openURL("https://www.grabulldirect.com/contact-us/");
            }
            callService()
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

    private fun callService(){
        try{
            if(Validation.isOnline(this)){
                var activeOrderRequest = ActiveOrderRequest()
                activeOrderRequest.deviceversion = Util.getVersionName(this)
                activeOrderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                // println("request date>>>>>> ${Util.getStringFromBean(reportRequest)}")
                fromSearch =false
                viewModel.getActiveOrderList(activeOrderRequest)
            }else{
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
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
                showSnackBar(binding.progressBar,getString(R.string.internet_connected))
                return
            }else if(binding.contentItem.searchEdittext.text.toString().isNullOrEmpty()){
                showSnackBar(binding.progressBar,"Please provide valid item Id")
                return
            }else{

                var orderSearchRequest = OrderSearchRequest()
                orderSearchRequest.deviceversion = Util.getVersionName(this)
                orderSearchRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderSearchRequest.order_id=binding.contentItem.searchEdittext.text.toString()
                // println("request date>>>>>> ${Util.getStringFromBean(reportRequest)}")
                fromSearch =true
                viewModel.getOrderSearch(orderSearchRequest)
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()
        addingItemAdapter.setOnItemClickListener(object :AddingItemAdapter.AddingTipsListener{
            override fun onItemClick(itemData:com.gb.restaurant.model.addtips.Data, v: View) {
                this@AddItemActivity.itemData =itemData
                addItemsViewDialog(itemData)
            }

        } )
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showSnackBar(binding.progressBar,it) }
        })
        viewModel.response.observe(this, Observer<ActiveOrderResponse> {
            it?.let {
                addingItemAdapter.notifyDataSetChanged()
                if(addingItemAdapter.itemCount >0){
                    binding.contentItem.addingItemsRecycler.visibility = View.VISIBLE
                    binding.contentItem.noOrderText.visibility = View.GONE}
                else{
                    binding.contentItem.addingItemsRecycler.visibility = View.GONE
                    binding.contentItem.noOrderText.visibility  = View.VISIBLE
                    if(fromSearch){
                        binding.contentItem.noOrderText.text=getString(R.string.oops_no_order_found_with_order_id_item)
                    }else{
                        binding.contentItem.noOrderText.text=getString(R.string.oops_no_order_found)
                    }
                   // no_order_text.text="${it.result}"
                }
            }
        })

        viewModel.addItemsOrderResponse.observe(this, Observer<AddOrderItemResponse> {
            it?.let {
               val cardProgress =  cardDialog?.findViewById<ProgressBar>(R.id.card_progress);
                println("add_item_res>> ${Util.getStringFromBean(it)}")
                if(it.status == Constant.STATUS.FAIL){
                    Util.alertDialog(it.result?:"",this)
                    cardProgress?.visibility =View.GONE
                    //showToast(it.result!!)
                }else{
                    if(it.result.equals("Items added successfully",true)){
                        cardDialog?.dismiss()
                        Util.alertDialog(it.result?:"",this)
                    }else{
                        cardProgress?.visibility =View.GONE
                        selOrderItemRequest?.newcard = "Yes"
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

    private fun addItemsViewDialog(itemData:com.gb.restaurant.model.addtips.Data) {
       var addOrderItemRequest =AddOrderItemRequest()
        var itemList = mutableListOf<com.gb.restaurant.model.Item>()
        var dialog = Dialog(this, R.style.AppCompatAlertDialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_items_layout)
        dialog.setCanceledOnTouchOutside(true)

        val titleText = dialog.findViewById<View>(R.id.title_text) as TextView
        titleText.text = itemData?.id
        val recyclerView = dialog.findViewById<View>(R.id.items_recycler) as RecyclerView
        val extraRecyclerView = dialog.findViewById<View>(R.id.extra_items_recycler) as RecyclerView
        val cancelImage = dialog.findViewById<View>(R.id.cancel_image_items) as ImageView
        val plusText = dialog.findViewById<View>(R.id.plus_text) as TextView
        val addItemButton = dialog.findViewById<View>(R.id.add_item_button) as TextView
        val itemEditText = dialog.findViewById<View>(R.id.item_text) as EditText
        val priceEditText = dialog.findViewById<View>(R.id.price_text) as EditText

        var myAdapter = ItemAdapter(this@AddItemActivity,itemList)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@AddItemActivity)
            //addItemDecoration(ListPaddingDecorationGray(this@HomeDetailActivity, 0,0))
            adapter = myAdapter
        }
        var extraItems= mutableListOf<String>()
        for (i in 0..5){
            extraItems.add("$i")
        }
        var gridAdapter = ExtraItemAdapter(this@AddItemActivity,extraItems)
        extraRecyclerView.apply {
            setHasFixedSize(true)
            this.layoutManager = GridLayoutManager(this@AddItemActivity,2)
            //addItemDecoration(ListPaddingDecorationGray(this@HomeDetailActivity, 0,0))
            this.adapter = gridAdapter
        }
        cancelImage.setOnClickListener {
            dialog.dismiss()
        }

        plusText.setOnClickListener {
            if(!itemEditText.text.isNullOrEmpty() && !priceEditText.text.isNullOrEmpty()){
                var item = com.gb.restaurant.model.Item(itemEditText.text.toString(),priceEditText.text.toString())
                itemList.add(item)
                println("data>>> ${Util.getStringFromBean(item)}")
                myAdapter.notifyDataSetChanged()
                itemEditText.setText("")
                priceEditText.setText("")
            }else{
                Util.alertDialog("Please add Items",this@AddItemActivity)
            }
        }
        addItemButton.setOnClickListener {
            if(itemList.isEmpty()) {
                if (itemEditText.text.isNullOrEmpty()) {
                    Util.alertDialog("Please add item.", this@AddItemActivity)
                    return@setOnClickListener
                }
                if (priceEditText.text.isNullOrEmpty()) {
                    Util.alertDialog("Please add Price.", this@AddItemActivity)
                    return@setOnClickListener
                }
                var item = com.gb.restaurant.model.Item(itemEditText.text.toString(),priceEditText.text.toString())
                itemList.add(item)
                myAdapter.notifyDataSetChanged()
            }else{
                if (!itemEditText.text.isNullOrEmpty() && !priceEditText.text.isNullOrEmpty()) {
                    var item = com.gb.restaurant.model.Item(itemEditText.text.toString(),priceEditText.text.toString())
                    itemList.add(item)
                    myAdapter.notifyDataSetChanged()
                }else if(itemEditText.text.isNullOrEmpty() && priceEditText.text.isNullOrEmpty()){
                    //do nothing
                }else{
                    if (itemEditText.text.isNullOrEmpty()) {
                        Util.alertDialog("Please add item.", this@AddItemActivity)
                        return@setOnClickListener
                    }
                    if (priceEditText.text.isNullOrEmpty()) {
                        Util.alertDialog("Please add Price.", this@AddItemActivity)
                        return@setOnClickListener
                    }
                }
            }
            println("data>>> ${Util.getStringFromBean(itemList)}")
            if(itemList.isNotEmpty()){
                if(myAdapter.getPriceCount() <=500) {
                    addOrderItemRequest.itemslist =itemList
                }else{
                    Util.alertDialog("Item(s) price should be maximum $500",this@AddItemActivity)
                    return@setOnClickListener
                }
               // if(itemData?.payby.equals("cash",true) || itemData?.payby.equals("Card ",true) ){
                    addOrderItemRequest.newcard = "No"
                    selOrderItemRequest = addOrderItemRequest
                    callAddItemsService(addOrderItemRequest)
                    dialog.dismiss()
//                }else{
//                    addOrderItemRequest.newcard = "Yes"
//                    selOrderItemRequest = addOrderItemRequest
//                    showCardDetailDialog()
//                    dialog.dismiss()
//                }
            }else{
                Util.alertDialog("Please add Items",this@AddItemActivity)
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
            val titleDialogText = this.findViewById<TextView>(R.id.title_dialog_text)
            val cardEditText = this.findViewById<EditText>(R.id.card_edittext)
            val expEditText = this.findViewById<EditText>(R.id.exp_edittext)
            val cvvEditText = this.findViewById<EditText>(R.id.cvv_edittext)
            val zipEditText = this.findViewById<EditText>(R.id.zip_edittext)
            val cardProgressBar = this.findViewById<ProgressBar>(R.id.card_progress)
            val cardHolderEditText = this.findViewById<EditText>(R.id.card_holder_edittext)
            val closeDialog = this.findViewById<ImageView>(R.id.close_dialog)
            val continueButton = this.findViewById<Button>(R.id.continue_button)
            titleDialogText.text = "Enter Card Detail"
            closeDialog.setOnClickListener {
                this.dismiss()
            }
            continueButton.setOnClickListener {
                if(cardEditText.text.toString().isNullOrEmpty() || cardEditText.text.length < 15){
                    Util.alertDialog("Please add Valid Card Number",this@AddItemActivity)
                    return@setOnClickListener
                }
                if(expEditText.text.toString().isNullOrEmpty() || expEditText.text.length <4){
                    Util.alertDialog("Please add Valid Expiry Date",this@AddItemActivity)
                    return@setOnClickListener
                }
                if(cvvEditText.text.toString().isNullOrEmpty() || cvvEditText.text.length <3){
                    Util.alertDialog("Please add Valid CVV",this@AddItemActivity)
                    return@setOnClickListener
                }
                if(zipEditText.text.toString().isNullOrEmpty() || zipEditText.text.length <5){
                    Util.alertDialog("Please add Valid Zip Code",this@AddItemActivity)
                    return@setOnClickListener
                }
                if(cardHolderEditText.text.toString().isNullOrEmpty()){
                    Util.alertDialog("Please add card holder name",this@AddItemActivity)
                    return@setOnClickListener
                }
                cardProgressBar.visibility = View.VISIBLE
                cardDialog = this
                selOrderItemRequest?.card = cardEditText.text.toString()
                selOrderItemRequest?.expiry = expEditText.text.toString()
                selOrderItemRequest?.cvv = cvvEditText.text.toString()
                selOrderItemRequest?.billingzip = zipEditText.text.toString()
                selOrderItemRequest?.cardholder = cardHolderEditText.text.toString()
                callAddItemsService(selOrderItemRequest!!)
                //this.dismiss()
            }
        }

    }

    private fun callAddItemsService(addOrderItemRequest :AddOrderItemRequest){//itemList :MutableList<com.gb.restaurantconnect.model.Item>
        try{
            if(Validation.isOnline(this)){
                //var addOrderItemRequest = AddOrderItemRequest()
                addOrderItemRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                addOrderItemRequest.order_id= itemData?.orderid!!
                addOrderItemRequest.deviceversion = Util.getVersionName(this)
                println("request add item>>>> ${Util.getStringFromBean(addOrderItemRequest)}")
                viewModel.addItemsOrder(addOrderItemRequest)
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
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

}
