package com.gb.restaurant.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.DialogBehavior
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.ModalDialog
import com.afollestad.materialdialogs.customview.customView
import com.gb.restaurant.CATPrintSDK.Canvas
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.ActivityHomeBinding
import com.gb.restaurant.databinding.HomeDetailActivityBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.PrinterModel
import com.gb.restaurant.model.additem.AddOrderItemRequest
import com.gb.restaurant.model.additem.AddOrderItemResponse
import com.gb.restaurant.model.addordertips.OrderTipsRequest
import com.gb.restaurant.model.addordertips.OrderTipsResponse
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.model.order.Item
import com.gb.restaurant.model.order.OrderRequest
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.orderdetail.OrderDetailRequest
import com.gb.restaurant.model.orderdetail.OrderDetailResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.ItemAdapter
import com.gb.restaurant.ui.adapter.NewDetailAdapter
import com.gb.restaurant.ui.adapter.PrinterListAdapter
import com.gb.restaurant.ui.fragments.BaseFragment
import com.gb.restaurant.utils.BluetoothDiscovery
import com.gb.restaurant.utils.IpScanner
import com.gb.restaurant.utils.Util
import com.gb.restaurant.utils.Utils
import com.gb.restaurant.viewmodel.TipsViewModel
import com.gb.restaurant.session.SessionManager


class HomeDetailActivity : BaseActivity() {

    private lateinit var newDetailAdapter: NewDetailAdapter
    private lateinit var list: MutableList<Item>
    private var data: Data? = null
    private lateinit var viewModel: TipsViewModel
    var rsLoginResponse: RsLoginResponse? = null
    private var materialDialog: MaterialDialog? = null
    private var selOrderItemRequest: AddOrderItemRequest? = null
    private var selOrderTipsRequest: OrderTipsRequest? = null
    private var cardDialog: MaterialDialog? = null
    private var fromAddItemOrTip = false

    public var mCanvas: Canvas? = null
    public var canvasBitmap: Bitmap? = null
    public var mBitmap: Bitmap? = null

    var sessionManager: SessionManager? = null
    private lateinit var binding: HomeDetailActivityBinding

    companion object {
        private val TAG: String = HomeDetailActivity::class.java.simpleName
        private val CONFIRM_PAGE: Int = 11
        val FROMPAGE: String = "FROMPAGE"
    }

    var fromPage: Int = 0// 0 from completed,new and schedule , 1 from active
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor()
       // setContentView(R.layout.home_detail_activity)
        binding = HomeDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        initData()
        initView()
    }

    private fun initData() {
        try {
            rsLoginResponse = MyApp.instance.rsLoginResponse
            intent.apply {
                fromPage = this.getIntExtra(FROMPAGE, 0)
            }
            viewModel = createViewModel()
            createItmList()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun createItmList() {
        try {
            list = mutableListOf()
            data = MyApp.instance.data
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun setStatusBarColor() {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                window.statusBarColor = ContextCompat.getColor(
                    this,
                    R.color.bg_color
                ); //status bar or the time bar at the top
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initView() {
        try {
            binding.apply {
                setSupportActionBar(toolbar)
                toolbar.navigationIcon = ContextCompat.getDrawable(this@HomeDetailActivity, R.drawable.ic_back)
                toolbar.setNavigationOnClickListener { onBackPressed() }
                titleHome.setOnClickListener { onBackPressed() }
                toolbar.title = ""// "${getString(R.string.back)}"//"${rsLoginResponse?.data?.name}"
                toolbar.setTitleTextColor(ContextCompat.getColor(this@HomeDetailActivity, R.color.colorPrimaryDark_one));

            }

            attachObserver()
            newDetailAdapter = NewDetailAdapter(this, data?.items as MutableList<Item>)
            binding.contentHomeDetail.apply {
                orderDetailItem.conUpdateButton.text = "Update Order status"
                detailRecycler.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@HomeDetailActivity)
                    // addItemDecoration(ListPaddingDecoration(this@ViewDialogActivity, 0,0))
                    adapter = newDetailAdapter
                }
                orderDetailItem.conUpdateButton.visibility = View.VISIBLE
                data?.name?.let {
                    orderDetailItem.nameText.text = "$it"
                }
                if (data?.payment!!.contains("Paid", true)) {
                    orderDetailItem.prepaidText.text = "PREPAID"
                    orderDetailItem.prepaidText.setBackgroundColor(ContextCompat.getColor(this@HomeDetailActivity, R.color.green))
                    orderDetailItem.deliveryTypeText.setTextColor(ContextCompat.getColor(this@HomeDetailActivity, R.color.green))
                    orderDetailItem.dontChargeText.visibility = View.VISIBLE
                } else {
                    orderDetailItem.prepaidText.text = "CASH"
                    orderDetailItem.prepaidText.setBackgroundColor(
                        ContextCompat.getColor(
                            this@HomeDetailActivity,
                            R.color.colorPrimaryDark_one
                        )
                    )
                    orderDetailItem.deliveryTypeText.setTextColor(ContextCompat.getColor(this@HomeDetailActivity, R.color.colorAccent))
                    orderDetailItem.dontChargeText.visibility = View.INVISIBLE
                }
                orderDetailItem.deliveryTypeText.text = "${data?.type!!.toUpperCase()}"

                if (!data?.holddate2.isNullOrEmpty()) {
                    futureOrderLayout.visibility = View.VISIBLE
                    if (data!!.type.equals("Delivery", true)) {
                        holdTimeText.text = "Hold Order: Delivery Time: ${data!!.holddate2}"
                    } else {
                        holdTimeText.text = "Hold Order: Pickup Time: ${data!!.holddate2}"
                    }
                } else {
                    futureOrderLayout.visibility = View.GONE
                    holdTimeText.visibility = View.GONE
                }

                if (!data?.type.isNullOrEmpty() && data?.type!!.contains("Pickup", true)) {
                    orderDetailItem.addressLayout.visibility = View.INVISIBLE
                    detailHomeFooter.deliveryFeeHomeLayout.visibility = View.GONE
                } else {
                    orderDetailItem.addressLayout.visibility = View.VISIBLE
                    detailHomeFooter.deliveryFeeHomeLayout.visibility = View.VISIBLE
                }
                data?.rewards?.let {reward->
                    if(reward > 0){
                        detailHomeFooter.rewardLayout.visibility = View.VISIBLE
                    }
                }
                /*if(!data?.type.isNullOrEmpty() && !data?.payment.isNullOrEmpty()){ //pending- cash(not paid)
                    var paymentStatus = ""
                  if(data?.payment!!.contains("pending",true)){
                      paymentStatus =  "NOT PAID"
                      delivery_type_text.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                    }else{
                      paymentStatus =    "PAID"
                      delivery_type_text.setTextColor(ContextCompat.getColor(this, R.color.green))
                    }
                    delivery_type_text.text = "${data?.type!!.toUpperCase()} $paymentStatus"
                }*/

                /*if(!data?.status.isNullOrEmpty()){
                    status.text = "${data?.status}"
                }*/
                if (!data?.delivery.isNullOrEmpty()) {
                    orderDetailItem.deliveryAddress.text = "${data?.delivery}"
                }
                if (!data?.deliverycharge.isNullOrEmpty()) {
                    data?.deliverycharge?.let {
                        detailHomeFooter.deliveryFeeText.text = "$${data?.deliverycharge}"
                    }
                }
                if (!data?.mobile.isNullOrEmpty()) {
                    orderDetailItem.phoneText.text = "Customer Ph: ${data?.mobile}"
                }


                if (data!!.items.isNullOrEmpty()) {
                    ordersItemCount.text = "Order(0 items)"
                } else {
                    ordersItemCount.text = "Order(${data!!.items!!.size} items)"
                }


                orderIdText.text = "ORDER # ${data!!.id}"
                if (data?.subtotal != null) {
                    detailHomeFooter.subTotalText.text = "$${data?.subtotal}"
                }
                if (!data?.offeramount.isNullOrEmpty()) {
                    data?.offeramount?.let {
                        detailHomeFooter.discountTaxt.text = "Discount-$$it"
                    }
                }
                if (data?.tax != null) {
                    detailHomeFooter.taxText.text = "$${data?.tax}"
                }
                if (data?.tip != null) {
                    detailHomeFooter.tipText.text = "$${data?.tip}"
                }
                if (data?.total != null) {
                    detailHomeFooter.totalTax.text = "Total $${data?.total}"
                }
                if (!data?.tip2.isNullOrEmpty()) {
                    detailHomeFooter.tipTwoText.text = "Tips $${data?.tip2}"
                } else {
                    detailHomeFooter.tipTwoText.text = "Tips_____"
                }
                if (!data?.date2.isNullOrEmpty()) {
                    orderTimeText.text = "ORDER TIME: ${data?.date2}"
                } else {
                    orderTimeText.text = ""
                }

                callOrderDetailService()

                orderDetailItem.txtPrint.isEnabled = true

                orderDetailItem.txtPrint.setOnClickListener {

                    orderDetailItem.txtPrint.isEnabled = false
                    Handler().postDelayed(Runnable {
                        orderDetailItem.txtPrint.isEnabled = true
                    }, 10000)

                    if (ActivityCompat.checkSelfPermission(
                            this@HomeDetailActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this@HomeDetailActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@HomeDetailActivity,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), 1
                        )
                    } else {


                        if (Utils.isLocationEnabled(this@HomeDetailActivity)!!) {

                            if (sessionManager!!.getPrinterAddress().isNotEmpty()) {
                                PrintingTask().execute()
                            } else {
                                showDialog(this@HomeDetailActivity, data!!)
                            }


                        } else {

                            AlertDialog.Builder(this@HomeDetailActivity)
                                .setMessage("Location is off")
                                .setPositiveButton(
                                    "Turn On"
                                ) { _, _ ->
                                    this@HomeDetailActivity.startActivity(
                                        Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                        )
                                    )
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }

                    }
                }


            }


        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    override fun onResume() {
        super.onResume()
        /* if (window != null) {
             val params = window.attributes
             params.width = LinearLayout.LayoutParams.MATCH_PARENT
             params.height = LinearLayout.LayoutParams.MATCH_PARENT
             params.horizontalMargin = 100.0f
             params.verticalMargin = 100.0f
             window.attributes = params as android.view.WindowManager.LayoutParams
         }*/
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (Utils.isLocationEnabled(this)!!) {

                    if (sessionManager!!.getPrinterAddress().isNotEmpty()) {
                        PrintingTask().execute()
                    } else {
                        showDialog(this, MyApp.instance.data!!)
                    }
                } else {

                    AlertDialog.Builder(this)
                        .setMessage("Location is off")
                        .setPositiveButton(
                            "Turn On"
                        ) { _, _ ->
                            this.startActivity(
                                Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                )
                            )
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }


            }
        }


    }


    fun confirmMethod(view: View) {
        try {
            showDeliveryDialog(data!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closePage(view: View) {
        try {
            onBackPressed()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }


    fun addItems(view: View) {
        try {
            addItemsViewDialog()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun tips(view: View) {
        try {
            showCustomViewDialog(data)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Activity.RESULT_OK) {
            if (requestCode == CONFIRM_PAGE) {
                finish()
            }
        }
    }

    private fun showCustomViewDialog(data: Data?) {
        println("data>>>>>>> ${Util.getStringFromBean(data!!)}")
        var orderTipsRequest = OrderTipsRequest()
        var dialog = Dialog(this, R.style.AppCompatAlertDialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.tips_layout)
        dialog.setCanceledOnTouchOutside(true)
        val titleHeader = dialog.findViewById<View>(R.id.title_header_text) as TextView
        val cancelImage = dialog.findViewById<View>(R.id.cancel_image) as ImageView
        val addTipsButton = dialog.findViewById<View>(R.id.add_tips_button) as TextView
        val tipsEdit = dialog.findViewById<View>(R.id.tips_edit) as EditText
        titleHeader.text = "${data?.id}"
        cancelImage.setOnClickListener {
            dialog.dismiss()
        }
        addTipsButton.setOnClickListener {
            if (!tipsEdit.text.isNullOrEmpty()) {
                orderTipsRequest.order_tips = tipsEdit.text.toString()
                orderTipsRequest.newcard = "No"
                selOrderTipsRequest = orderTipsRequest
                callTipsService(orderTipsRequest)
                dialog.dismiss()
            } else {
                showToast("Please add Tips")
            }
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dialog.show();

    }

    private fun callTipsService(orderTipsRequest: OrderTipsRequest) {
        try {
            if (Validation.isOnline(this)) {
                orderTipsRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderTipsRequest.order_id = data?.id!!
                orderTipsRequest.deviceversion = Util.getVersionName(this)
                println("tips request>>>>>>> ${Util.getStringFromBean(orderTipsRequest)}")
                viewModel.addTips(orderTipsRequest)
            } else {
                showToast(getString(R.string.internet_connected))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun addItemsViewDialog() {
        var addOrderItemRequest = AddOrderItemRequest()
        var itemList = mutableListOf<com.gb.restaurant.model.Item>()
        var dialog = Dialog(this, R.style.AppCompatAlertDialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_items_layout)
        dialog.setCanceledOnTouchOutside(true)
        val titleText = dialog.findViewById<View>(R.id.title_text) as TextView
        titleText.text = data?.id
        val recyclerView = dialog.findViewById<View>(R.id.items_recycler) as RecyclerView
        val cancelImage = dialog.findViewById<View>(R.id.cancel_image_items) as ImageView
        val plusText = dialog.findViewById<View>(R.id.plus_text) as TextView
        val addItemButton = dialog.findViewById<View>(R.id.add_item_button) as TextView
        val itemEditText = dialog.findViewById<View>(R.id.item_text) as EditText
        val priceEditText = dialog.findViewById<View>(R.id.price_text) as EditText

        var myAdapter = ItemAdapter(this@HomeDetailActivity, itemList)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HomeDetailActivity)
            //addItemDecoration(ListPaddingDecorationGray(this@HomeDetailActivity, 0,0))
            adapter = myAdapter
        }
        cancelImage.setOnClickListener {
            dialog.dismiss()
        }

        plusText.setOnClickListener {
            if (!itemEditText.text.isNullOrEmpty() && !priceEditText.text.isNullOrEmpty()) {
                var item = com.gb.restaurant.model.Item(
                    itemEditText.text.toString(),
                    priceEditText.text.toString()
                )
                itemList.add(item)
                println("data>>> ${Util.getStringFromBean(item)}")
                myAdapter.notifyDataSetChanged()
                itemEditText.setText("")
                priceEditText.setText("")
            } else {
                Util.alertDialog("Please add Items", this@HomeDetailActivity)
            }
        }
        addItemButton.setOnClickListener {
            if (itemList.isEmpty()) {
                if (itemEditText.text.isNullOrEmpty()) {
                    Util.alertDialog("Please add item.", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                if (priceEditText.text.isNullOrEmpty()) {
                    Util.alertDialog("Please add Price.", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                var item = com.gb.restaurant.model.Item(
                    itemEditText.text.toString(),
                    priceEditText.text.toString()
                )
                itemList.add(item)
                myAdapter.notifyDataSetChanged()
            } else {
                if (!itemEditText.text.isNullOrEmpty() && !priceEditText.text.isNullOrEmpty()) {
                    var item = com.gb.restaurant.model.Item(
                        itemEditText.text.toString(),
                        priceEditText.text.toString()
                    )
                    itemList.add(item)
                    myAdapter.notifyDataSetChanged()
                } else if (itemEditText.text.isNullOrEmpty() && priceEditText.text.isNullOrEmpty()) {
                    //do nothing
                } else {
                    if (itemEditText.text.isNullOrEmpty()) {
                        Util.alertDialog("Please add item.", this@HomeDetailActivity)
                        return@setOnClickListener
                    }
                    if (priceEditText.text.isNullOrEmpty()) {
                        Util.alertDialog("Please add Price.", this@HomeDetailActivity)
                        return@setOnClickListener
                    }
                }
            }
            println("data>>> ${Util.getStringFromBean(itemList)}")
            if (itemList.isNotEmpty()) {
                if (myAdapter.getPriceCount() <= 500) {
                    addOrderItemRequest.itemslist = itemList
                } else {
                    Util.alertDialog(
                        "Item(s) price should be maximum $500",
                        this@HomeDetailActivity
                    )
                    return@setOnClickListener
                }
                addOrderItemRequest.newcard = "No"
                selOrderItemRequest = addOrderItemRequest
                callAddItemsService(addOrderItemRequest)
                dialog.dismiss()
            } else {
                Util.alertDialog("Please add Items", this@HomeDetailActivity)
            }
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dialog.show();
    }

    private fun showCardDetailDialog(data: Data, fromItem: Boolean = true) {
        // dialogBehavior: DialogBehavior = ModalDialog
        val dialog = MaterialDialog(this).show {
            this.cancelOnTouchOutside(false)
            cornerRadius(null, R.dimen.dimen_30)
            customView(
                R.layout.card_detail_layout,
                scrollable = false,
                noVerticalPadding = true,
                horizontalPadding = false
            )
            val titleDialogText = this.findViewById<TextView>(R.id.title_dialog_text)
            val cardEditText = this.findViewById<EditText>(R.id.card_edittext)
            val expEditText = this.findViewById<EditText>(R.id.exp_edittext)
            val cvvEditText = this.findViewById<EditText>(R.id.cvv_edittext)
            val zipEditText = this.findViewById<EditText>(R.id.zip_edittext)
            val cardProgressBar = this.findViewById<ProgressBar>(R.id.card_progress)
            val cardHolderEditText = this.findViewById<EditText>(R.id.card_holder_edittext)
            val closeDialog = this.findViewById<ImageView>(R.id.close_dialog)
            val continueButton = this.findViewById<Button>(R.id.continue_button)
            titleDialogText.text = "Enter Card Details"
            closeDialog.setOnClickListener {
                this.dismiss()
            }
            continueButton.setOnClickListener {
                if (cardEditText.text.toString()
                        .isNullOrEmpty() || cardEditText.text.length < 15
                ) {
                    Util.alertDialog("Please add Valid Card Number", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                if (expEditText.text.toString().isNullOrEmpty() || expEditText.text.length < 4) {
                    Util.alertDialog("Please add Valid Expiry Date", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                if (cvvEditText.text.toString().isNullOrEmpty() || cvvEditText.text.length < 3) {
                    Util.alertDialog("Please add Valid CVV", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                if (zipEditText.text.toString().isNullOrEmpty() || zipEditText.text.length < 5) {
                    Util.alertDialog("Please add Valid Zip Code", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                if (cardHolderEditText.text.toString().isNullOrEmpty()) {
                    Util.alertDialog("Please add card holder name", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                cardProgressBar.visibility = View.VISIBLE
                cardDialog = this
                if (fromItem) {
                    selOrderItemRequest?.card = cardEditText.text.toString()
                    selOrderItemRequest?.expiry = expEditText.text.toString()
                    selOrderItemRequest?.cvv = cvvEditText.text.toString()
                    selOrderItemRequest?.billingzip = zipEditText.text.toString()
                    selOrderItemRequest?.cardholder = cardHolderEditText.text.toString()
                    callAddItemsService(selOrderItemRequest!!)
                } else {
                    selOrderTipsRequest?.card = cardEditText.text.toString()
                    selOrderTipsRequest?.expiry = expEditText.text.toString()
                    selOrderTipsRequest?.cvv = cvvEditText.text.toString()
                    selOrderTipsRequest?.billingzip = zipEditText.text.toString()
                    selOrderTipsRequest?.cardholder = cardHolderEditText.text.toString()
                    callTipsService(selOrderTipsRequest!!)
                }
                //this.dismiss()
            }
        }

    }


    private fun callOrderDetailService() {
        try {
            if (Validation.isOnline(this)) {
                var orderDetailRequest = OrderDetailRequest()
                orderDetailRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderDetailRequest.order_id = data!!.id!!
                orderDetailRequest.deviceversion = Util.getVersionName(this)
                println("activerequest>>> ${Util.getStringFromBean(orderDetailRequest)}")
                viewModel.getOrderDetailResponse(orderDetailRequest)
            } else {
                showToast(getString(R.string.internet_connected))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun callAddItemsService(addOrderItemRequest: AddOrderItemRequest) {//itemList :MutableList<com.gb.restaurantconnect.model.Item>
        try {
            if (Validation.isOnline(this)) {
                //var addOrderItemRequest = AddOrderItemRequest()
                addOrderItemRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                addOrderItemRequest.order_id = data?.orderid!!
                addOrderItemRequest.deviceversion = Util.getVersionName(this)
                println("request add item>>>> ${Util.getStringFromBean(addOrderItemRequest)}")
                viewModel.addItemsOrder(addOrderItemRequest)
            } else {
                showToast(getString(R.string.internet_connected))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun showDeliveryDialog(data: Data, dialogBehavior: DialogBehavior = ModalDialog) {
        val dialog = MaterialDialog(this, dialogBehavior).show {
            this.cancelOnTouchOutside(false)
            cornerRadius(null, R.dimen.dimen_30)
            customView(
                R.layout.delivery_paid_layout,
                scrollable = false,
                noVerticalPadding = true,
                horizontalPadding = false
            )
            val titleDialogText = this.findViewById<TextView>(R.id.title_dialog_text)
            val deliveryButton = this.findViewById<Button>(R.id.delivery_button)
            val cancelButton = this.findViewById<Button>(R.id.cancel_button)
            val closeDialog = this.findViewById<ImageView>(R.id.close_dialog)
            if (data.type.equals("Delivery", true)) {
                //order_text.text = "DELIVERY PAID"
                deliveryButton.text = "DELIVERED"
            } else {
                // order_text.text = "PICKUP PAID"
                deliveryButton.text = "PICKED UP"
            }
            titleDialogText.text = "${data.id}"
            closeDialog.setOnClickListener {
                this.dismiss()
            }
            cancelButton.setOnClickListener {
                this.dismiss()
                var orderStatusRequest = OrderStatusRequest()
                orderStatusRequest.deviceversion = Util.getVersionName(this@HomeDetailActivity)
                orderStatusRequest.status = Constant.ORDER_STATUS.CANCEL
                orderStatusRequest.order_id = data.id!!
                callOrderStatusService(orderStatusRequest)
            }
            deliveryButton.setOnClickListener {
                this.dismiss()
                var orderStatusRequest = OrderStatusRequest()
                orderStatusRequest.deviceversion = Util.getVersionName(this@HomeDetailActivity)
                if (data.type.equals("Delivery", true)) {
                    orderStatusRequest.status = Constant.ORDER_STATUS.DELIVERED
                } else {
                    orderStatusRequest.status = Constant.ORDER_STATUS.PICKEDUP
                }

                orderStatusRequest.order_id = data.id!!
                callOrderStatusService(orderStatusRequest)
            }

        }
    }

    private fun callOrderStatusService(orderStatusRequest: OrderStatusRequest) {
        try {
            if (Validation.isOnline(this)) {
                orderStatusRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                viewModel.orderStatus(orderStatusRequest)
            } else {
                showToast(getString(R.string.internet_connected))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(BaseFragment.TAG, e.message!!)
        }
    }


    private fun attachObserver() {
        val  cardProgress=cardDialog?.findViewById<ProgressBar>(R.id.card_progress)
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showToast(it) }
        })
        viewModel.addTipsResponse.observe(this, Observer<OrderTipsResponse> {
            it?.let {

                println("tipsResponse>>>>>>>>> ${Util.getStringFromBean(it)}")
                if (it.status == Constant.STATUS.FAIL) {
                    // showToast(it.result!!)
                    Util.alertDialog(it.result ?: "", this)
                    cardProgress?.visibility = View.GONE
                } else {
                    if (it.result.equals(
                            "Tips charged successfully",
                            true
                        ) || it.result.equals("Tips added successfully", true)
                    ) {
                        cardDialog?.dismiss()
                        fromAddItemOrTip = true
                        Util.alertDialog(it.result ?: "", this)
                        callOrderDetailService()
                    } else if (it.result.equals("Tips Already Added", true)) {
                        cardDialog?.dismiss()
                        fromAddItemOrTip = true
                        Util.alertDialog(it.result ?: "", this)
                    } else {
                        cardProgress?.visibility = View.GONE
                        selOrderTipsRequest?.newcard = "Yes"
                        if (cardDialog == null) {
                            showCardDetailDialog(data!!, fromItem = false)
                        } else {
                            if (!cardDialog!!.isShowing) {
                                showCardDetailDialog(data!!, fromItem = false)
                            }
                        }
                        if (it.data?.payment.equals("Failed"))
                            Util.alertDialog(it.data?.result ?: "", this)
                    }

                }
            }
        })

        viewModel.addItemsOrderResponse.observe(this, Observer<AddOrderItemResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                    Util.alertDialog(it?.result ?: "", this)
                    cardProgress?.visibility = View.GONE
                } else {
                    if (it.result.equals("Items added successfully", true)) {
                        fromAddItemOrTip = true
                        cardDialog?.dismiss()
                        Util.alertDialog(it.result ?: "", this)
                        callOrderDetailService()
                    } else {
                        cardProgress?.visibility = View.GONE
                        selOrderItemRequest?.newcard = "Yes"
                        if (cardDialog == null) {
                            showCardDetailDialog(data!!, fromItem = true)
                        } else {
                            if (!cardDialog!!.isShowing) {
                                showCardDetailDialog(data!!, fromItem = true)
                            }
                        }
                        if (it.data?.payment.equals("Failed"))
                            Util.alertDialog(it.data?.result ?: "", this)
                    }
                }
            }
        })

        viewModel.orderResponse.observe(this, Observer<OrderResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                    showToast(it.result!!)
                } else {
                    showToast(it.result!!)
                }
            }
        })

        viewModel.orderDetailResponse.observe(this, Observer<OrderDetailResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                    showToast(it.result!!)
                } else {
                    if (it.data != null) {
                        MyApp.instance.data = it.data
                        setValue(it.data)
                        createItmList()
                        newDetailAdapter.addAll(it.data?.items as List<Item>)
                    }
                }
            }
        })
        viewModel.orderStatusResponse.observe(this, Observer<OrderStatusResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                    showToast(it.result!!)
                } else {
                    showToast(it.result!!)
                    if (materialDialog != null) {
                        materialDialog!!.dismiss()
                    }
                    finishPage()
                    //callService()
                }
            }
        })


    }

    private fun setValue(data: Data) {
        binding.contentHomeDetail.detailHomeFooter.apply {
            try {
                if (!data?.offeramount.isNullOrEmpty()) {
                    data?.offeramount?.let {
                        discountTaxt.text =
                            "Discount-$${String.format("%.2f", data?.offeramount!!.toFloat())}"
                    }
                }
                if (!data.tip2.isNullOrEmpty()) {
                    tipTwoText.text =
                        "Tips $${String.format("%.2f", data.tip2!!.toFloat())}"
                } else {
                    tipTwoText.text = "Tips_____"
                }
                if (data.subtotal != null) {
                    subTotalText.text = "$${data.subtotal}"
                }
                if (data.total != null) {
                    totalTax.text = "Total $${data.total}"
                }
                if (data?.tax != null) {
                    taxText.text = "$${String.format("%.2f", data?.tax!!.toFloat())}"
                }
                if (data?.tip != null) {
                    tipText.text = "$${String.format("%.2f", data?.tip!!.toFloat())}"
                }
                if (!data?.deliverycharge.isNullOrEmpty()) {
                    data?.deliverycharge?.let {
                        deliveryFeeText.text =
                            "$${String.format("%.2f", data?.deliverycharge!!.toFloat())}"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, e.message!!)
            }
        }

    }

    private fun finishPage() {
        try {
            var intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun callService() {
        try {
            if (Validation.isOnline(this)) {
                var orderRequest = OrderRequest()
                orderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderRequest.service_type = Constant.SERVICE_TYPE.GET_ACTIVE_ORDER
                orderRequest.deviceversion = Util.getVersionName(this)
                println("activeresponse>>> ${Util.getStringFromBean(orderRequest)}")
                viewModel.getOrderResponse(orderRequest)
            } else {
                showSnackBar(binding.progressBar, getString(R.string.internet_connected))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(BaseFragment.TAG, e.message!!)
        }
    }


    private fun createViewModel(): TipsViewModel =
        ViewModelProviders.of(this).get(TipsViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (fromAddItemOrTip) {
            setResult(Activity.RESULT_OK)
            finishPage()
        } else {
            super.onBackPressed()
        }
    }

    private fun showDialog(mContext: Context, data: Data) {

        val printerList = ArrayList<PrinterModel>()
        lateinit var bluetoothDiscovery: BluetoothDiscovery
        lateinit var ipScanner: IpScanner

        val dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.printer_selection_dialog)

        val rgPrinterType = dialog.findViewById(R.id.rgPrinterType) as RadioGroup
        val rbBluetooth = dialog.findViewById(R.id.rbBluetooth) as RadioButton
        val rbLAN = dialog.findViewById(R.id.rbLAN) as RadioButton


        val scanningProgressBar =
            dialog.findViewById(R.id.scanningProgressBar) as ProgressBar
        val recyclerView: RecyclerView = dialog.findViewById(R.id.recyclerView)

        var printerListAdapter = PrinterListAdapter(printerList)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = printerListAdapter

        printerListAdapter.onItemClick = { printerList ->

            // do something with your item
            Log.d("TAG", printerList.printer_id)

            dialog.dismiss()
            mCanvas = Canvas(canvasBitmap)
            mBitmap = Utils.createOrderReceipt(this, mCanvas, 576, data)
            if (mBitmap != null) {

                if (bluetoothDiscovery != null) {
                    bluetoothDiscovery.stopDiscovering()
                    bluetoothDiscovery.unregisterReceiver()
                }

                sessionManager!!.setPrinterAddress(printerList.printer_id)
                sessionManager!!.setPrinterType(printerList.printer_type)

                PrintingTask().execute()

            }
        }


        rgPrinterType.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radio: RadioButton = dialog.findViewById(checkedId)


                if (checkedId == R.id.rbBluetooth) {

                    try {
                        ipScanner.stop()
                        ipScanner.onSearchError("Stop")
                    } catch (e: java.lang.Exception) {
                    }

                    bluetoothDiscovery = BluetoothDiscovery(
                        mContext,
                        object : BluetoothDiscovery.BluetoothDiscoveryListener {
                            override fun discoveryStarted() {
                                scanningProgressBar.visibility = View.VISIBLE
                                printerList.clear();
                                runOnUiThread(Runnable { printerListAdapter.notifyDataSetChanged() })

                            }

                            override fun discoveryFinished() {
                                scanningProgressBar.visibility = View.INVISIBLE
                            }

                            override fun onBluetoothDeviceFound(mDeviceList: java.util.ArrayList<BluetoothDevice?>) {
                                //printerStatusModelArrayList = printerStatusAdapter.getList()

                                for (info in mDeviceList) {
                                    if (info != null) {
                                        if (ActivityCompat.checkSelfPermission(
                                                this@HomeDetailActivity,
                                                Manifest.permission.BLUETOOTH_CONNECT
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            return
                                        }
                                        if (info.name != null) {

                                            val devType = info.bluetoothClass.majorDeviceClass
                                            if (devType != BluetoothClass.Device.Major.IMAGING) {
                                                return
                                            }
                                            Utils.pairBluetoothDevice(info)

                                            var newPrinterModel = PrinterModel()
                                            newPrinterModel.printer_id = "" + info.address
                                            newPrinterModel.printer_name = info.name
                                            newPrinterModel.printer_type = 1
                                            if (!Utils.containsString(
                                                    "" + info.address,
                                                    printerList
                                                )
                                            ) {
                                                printerList.add(newPrinterModel)
                                                runOnUiThread(Runnable { printerListAdapter.notifyDataSetChanged() })
                                            }

                                        } else {
                                            return
                                        }
                                    }
                                }
                            }
                        })
                    bluetoothDiscovery.startDiscovering()
                } else if (checkedId == R.id.rbLAN) {

                    if (bluetoothDiscovery != null) {
                        bluetoothDiscovery.stopDiscovering()
                        bluetoothDiscovery.unregisterReceiver()
                    }

                    ipScanner = object : IpScanner() {
                        override fun onSearchStart() {
                            printerList.clear();
                            runOnUiThread(Runnable {
                                printerListAdapter.notifyDataSetChanged()
                                scanningProgressBar.visibility = View.VISIBLE
                            })
                        }

                        override fun onSearchFinish(devicesList: List<DeviceBean>?) {
                            for (info in devicesList!!) {
                                if (info != null) {
                                    if (info.deviceIp != null) {
                                        var newPrinterModel = PrinterModel()
                                        newPrinterModel.printer_id = "" + info.deviceIp
                                        newPrinterModel.printer_name = "" + info.macAddress
                                        newPrinterModel.printer_type = 2
                                        if (!Utils.containsString(
                                                "" + info.deviceIp,
                                                printerList
                                            )
                                        ) {
                                            printerList.add(newPrinterModel)
                                            runOnUiThread(Runnable {
                                                scanningProgressBar.visibility = View.INVISIBLE
                                                printerListAdapter.notifyDataSetChanged()
                                            })
                                        }

                                    } else {
                                        return
                                    }
                                }
                            }
                        }

                        override fun onSearchError(msg: String?) {
                            runOnUiThread(Runnable {
                                scanningProgressBar.visibility = View.INVISIBLE
                            })
                            scanningProgressBar.visibility = View.INVISIBLE
                        }
                    }
                    ipScanner.start()
                }
            })
        rbBluetooth.isChecked = true
        dialog.show()

    }


    fun print(printer_type: Int, printer_id: String) {

        mCanvas = Canvas(canvasBitmap)
        mBitmap = Utils.createOrderReceipt(this, mCanvas, 576, data)
        if (mBitmap != null) {

            //Print Munbyn
            Utils.munbynPrinting(
                this,
                mBitmap,
                printer_type,
                printer_id
            )

        }

    }

    inner class PrintingTask() : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {
            // ...

            print(
                sessionManager!!.getPrinterType(),
                sessionManager!!.getPrinterAddress()
            )

            return ""
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            // ...
        }
    }


}
