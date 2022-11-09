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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
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
import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.add_items_layout.*
import kotlinx.android.synthetic.main.card_detail_layout.*
import kotlinx.android.synthetic.main.content_add_tips.*
import kotlinx.android.synthetic.main.content_item.*
import kotlinx.android.synthetic.main.content_item.no_order_text
import kotlinx.android.synthetic.main.content_item.search_edittext
import kotlinx.android.synthetic.main.custom_appbar.*


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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        setContentView(R.layout.activity_add_item)
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
            back_layout.setOnClickListener {
                onBackPressed()
            }
            attachObserver()

            addingItemAdapter = AddingItemAdapter(this,viewModel)
            adding_items_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@AddItemActivity)
                adapter = addingItemAdapter
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
                fromSearch =false
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
            it?.let { showSnackBar(progress_bar,it) }
        })
        viewModel.response.observe(this, Observer<ActiveOrderResponse> {
            it?.let {
                addingItemAdapter.notifyDataSetChanged()
                if(addingItemAdapter.itemCount >0){
                    adding_items_recycler.visibility = View.VISIBLE
                    no_order_text.visibility = View.GONE}
                else{
                    adding_items_recycler.visibility = View.GONE
                    no_order_text.visibility = View.VISIBLE
                    if(fromSearch){
                        no_order_text.text=getString(R.string.oops_no_order_found_with_order_id_item)
                    }else{
                        no_order_text.text=getString(R.string.oops_no_order_found)
                    }
                   // no_order_text.text="${it.result}"
                }
            }
        })

        viewModel.addItemsOrderResponse.observe(this, Observer<AddOrderItemResponse> {
            it?.let {
                println("add_item_res>> ${Util.getStringFromBean(it)}")
                if(it.status == Constant.STATUS.FAIL){
                    Util.alertDialog(it.result?:"",this)
                    cardDialog?.card_progress?.visibility =View.GONE
                    //showToast(it.result!!)
                }else{
                    if(it.result.equals("Items added successfully",true)){
                        cardDialog?.dismiss()
                        Util.alertDialog(it.result?:"",this)
                    }else{
                        cardDialog?.card_progress?.visibility =View.GONE
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
            title_dialog_text.text = "Enter Card Detail"
            close_dialog.setOnClickListener {
                this.dismiss()
            }
            continue_button.setOnClickListener {
                if(card_edittext.text.toString().isNullOrEmpty() || card_edittext.text.length < 15){
                    Util.alertDialog("Please add Valid Card Number",this@AddItemActivity)
                    return@setOnClickListener
                }
                if(exp_edittext.text.toString().isNullOrEmpty() || exp_edittext.text.length <4){
                    Util.alertDialog("Please add Valid Expiry Date",this@AddItemActivity)
                    return@setOnClickListener
                }
                if(cvv_edittext.text.toString().isNullOrEmpty() || cvv_edittext.text.length <3){
                    Util.alertDialog("Please add Valid CVV",this@AddItemActivity)
                    return@setOnClickListener
                }
                if(zip_edittext.text.toString().isNullOrEmpty() || zip_edittext.text.length <5){
                    Util.alertDialog("Please add Valid Zip Code",this@AddItemActivity)
                    return@setOnClickListener
                }
                if(card_holder_edittext.text.toString().isNullOrEmpty()){
                    Util.alertDialog("Please add card holder name",this@AddItemActivity)
                    return@setOnClickListener
                }
                card_progress.visibility = View.VISIBLE
                cardDialog = this
                selOrderItemRequest?.card = card_edittext.text.toString()
                selOrderItemRequest?.expiry = exp_edittext.text.toString()
                selOrderItemRequest?.cvv = cvv_edittext.text.toString()
                selOrderItemRequest?.billingzip = zip_edittext.text.toString()
                selOrderItemRequest?.cardholder = card_holder_edittext.text.toString()
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
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

}
