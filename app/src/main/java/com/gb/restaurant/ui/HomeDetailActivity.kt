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
import com.grabull.session.SessionManager
import kotlinx.android.synthetic.main.card_detail_layout.*
import kotlinx.android.synthetic.main.content_home_detail.*
import kotlinx.android.synthetic.main.delivery_paid_layout.*
import kotlinx.android.synthetic.main.delivery_paid_layout.close_dialog
import kotlinx.android.synthetic.main.delivery_paid_layout.title_dialog_text
import kotlinx.android.synthetic.main.detail_com_footer.*
import kotlinx.android.synthetic.main.detail_home_footer.*
import kotlinx.android.synthetic.main.detail_home_footer.delivery_fee_text
import kotlinx.android.synthetic.main.detail_home_footer.discount_taxt
import kotlinx.android.synthetic.main.detail_home_footer.sub_total_text
import kotlinx.android.synthetic.main.detail_home_footer.tax_text
import kotlinx.android.synthetic.main.detail_home_footer.tip_text
import kotlinx.android.synthetic.main.detail_home_footer.tip_two_text
import kotlinx.android.synthetic.main.detail_home_footer.total_tax
import kotlinx.android.synthetic.main.home_detail_activity.*
import kotlinx.android.synthetic.main.order_detail_item.*


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


    companion object {
        private val TAG: String = HomeDetailActivity::class.java.simpleName
        private val CONFIRM_PAGE: Int = 11
        val FROMPAGE: String = "FROMPAGE"
    }

    var fromPage: Int = 0// 0 from completed,new and schedule , 1 from active
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor()
        setContentView(R.layout.home_detail_activity)
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
            setSupportActionBar(toolbar)
            toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            title_home.setOnClickListener { onBackPressed() }
            toolbar.title = ""// "${getString(R.string.back)}"//"${rsLoginResponse?.data?.name}"
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark_one));
            con_update_button.text = "Update Order status"
            attachObserver()
            newDetailAdapter = NewDetailAdapter(this, data?.items as MutableList<Item>)
            detail_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@HomeDetailActivity)
                // addItemDecoration(ListPaddingDecoration(this@ViewDialogActivity, 0,0))
                adapter = newDetailAdapter
            }
            con_update_button.visibility = View.VISIBLE
            data?.name?.let {
                name_text.text = "$it"
            }
            if (data?.payment!!.contains("Paid", true)) {
                prepaid_text.text = "PREPAID"
                prepaid_text.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                delivery_type_text.setTextColor(ContextCompat.getColor(this, R.color.green))
                dont_charge_text.visibility = View.VISIBLE
            } else {
                prepaid_text.text = "CASH"
                prepaid_text.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorPrimaryDark_one
                    )
                )
                delivery_type_text.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                dont_charge_text.visibility = View.INVISIBLE
            }
            delivery_type_text.text = "${data?.type!!.toUpperCase()}"

            if (!data?.holddate2.isNullOrEmpty()) {
                future_order_layout.visibility = View.VISIBLE
                if (data!!.type.equals("Delivery", true)) {
                    hold_time_text.text = "Hold Order: Delivery Time: ${data!!.holddate2}"
                } else {
                    hold_time_text.text = "Hold Order: Pickup Time: ${data!!.holddate2}"
                }
            } else {
                future_order_layout.visibility = View.GONE
                hold_time_text.visibility = View.GONE
            }

            if (!data?.type.isNullOrEmpty() && data?.type!!.contains("Pickup", true)) {
                address_layout.visibility = View.INVISIBLE
                delivery_fee_home_layout.visibility = View.GONE
            } else {
                address_layout.visibility = View.VISIBLE
                delivery_fee_home_layout.visibility = View.VISIBLE
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
                delivery_address.text = "${data?.delivery}"
            }
            if (!data?.deliverycharge.isNullOrEmpty()) {
                data?.deliverycharge?.let {
                    delivery_fee_text.text = "$${data?.deliverycharge}"
                }
            }
            if (!data?.mobile.isNullOrEmpty()) {
                phone_text.text = "Customer Ph: ${data?.mobile}"
            }


            if (data!!.items.isNullOrEmpty()) {
                orders_item_count.text = "Order(0 items)"
            } else {
                orders_item_count.text = "Order(${data!!.items!!.size} items)"
            }


            order_id_text.text = "ORDER # ${data!!.id}"
            if (data?.subtotal != null) {
                sub_total_text.text = "$${data?.subtotal}"
            }
            if (!data?.offeramount.isNullOrEmpty()) {
                data?.offeramount?.let {
                    discount_taxt.text = "Discount-$$it"
                }
            }
            if (data?.tax != null) {
                tax_text.text = "$${data?.tax}"
            }
            if (data?.tip != null) {
                tip_text.text = "$${data?.tip}"
            }
            if (data?.total != null) {
                total_tax.text = "Total $${data?.total}"
            }
            if (!data?.tip2.isNullOrEmpty()) {
                tip_two_text.text = "Tips $${data?.tip2}"
            } else {
                tip_two_text.text = "Tips_____"
            }
            if (!data?.date2.isNullOrEmpty()) {
                order_time_text.text = "ORDER TIME: ${data?.date2}"
            } else {
                order_time_text.text = ""
            }

            callOrderDetailService()

            txtPrint.isEnabled = true

            txtPrint.setOnClickListener {

                txtPrint.isEnabled = false
                Handler().postDelayed(Runnable {
                    txtPrint.isEnabled = true
                }, 10000)

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ), 1
                    )
                } else {


                    if (Utils.isLocationEnabled(this)!!) {

                        if (sessionManager!!.getPrinterAddress().isNotEmpty()) {
                            PrintingTask().execute()
                        } else {
                            showDialog(this, data!!)
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
            title_dialog_text.text = "Enter Card Details"
            close_dialog.setOnClickListener {
                this.dismiss()
            }
            continue_button.setOnClickListener {
                if (card_edittext.text.toString()
                        .isNullOrEmpty() || card_edittext.text.length < 15
                ) {
                    Util.alertDialog("Please add Valid Card Number", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                if (exp_edittext.text.toString().isNullOrEmpty() || exp_edittext.text.length < 4) {
                    Util.alertDialog("Please add Valid Expiry Date", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                if (cvv_edittext.text.toString().isNullOrEmpty() || cvv_edittext.text.length < 3) {
                    Util.alertDialog("Please add Valid CVV", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                if (zip_edittext.text.toString().isNullOrEmpty() || zip_edittext.text.length < 5) {
                    Util.alertDialog("Please add Valid Zip Code", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                if (card_holder_edittext.text.toString().isNullOrEmpty()) {
                    Util.alertDialog("Please add card holder name", this@HomeDetailActivity)
                    return@setOnClickListener
                }
                card_progress.visibility = View.VISIBLE
                cardDialog = this
                if (fromItem) {
                    selOrderItemRequest?.card = card_edittext.text.toString()
                    selOrderItemRequest?.expiry = exp_edittext.text.toString()
                    selOrderItemRequest?.cvv = cvv_edittext.text.toString()
                    selOrderItemRequest?.billingzip = zip_edittext.text.toString()
                    selOrderItemRequest?.cardholder = card_holder_edittext.text.toString()
                    callAddItemsService(selOrderItemRequest!!)
                } else {
                    selOrderTipsRequest?.card = card_edittext.text.toString()
                    selOrderTipsRequest?.expiry = exp_edittext.text.toString()
                    selOrderTipsRequest?.cvv = cvv_edittext.text.toString()
                    selOrderTipsRequest?.billingzip = zip_edittext.text.toString()
                    selOrderTipsRequest?.cardholder = card_holder_edittext.text.toString()
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
            if (data.type.equals("Delivery", true)) {
                //order_text.text = "DELIVERY PAID"
                delivery_button.text = "DELIVERED"
            } else {
                // order_text.text = "PICKUP PAID"
                delivery_button.text = "PICKED UP"
            }
            title_dialog_text.text = "${data.id}"
            close_dialog.setOnClickListener {
                this.dismiss()
            }
            cancel_button.setOnClickListener {
                this.dismiss()
                var orderStatusRequest = OrderStatusRequest()
                orderStatusRequest.deviceversion = Util.getVersionName(this@HomeDetailActivity)
                orderStatusRequest.status = Constant.ORDER_STATUS.CANCEL
                orderStatusRequest.order_id = data.id!!
                callOrderStatusService(orderStatusRequest)
            }
            delivery_button.setOnClickListener {
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
                    cardDialog?.card_progress?.visibility = View.GONE
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
                        cardDialog?.card_progress?.visibility = View.GONE
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
                    cardDialog?.card_progress?.visibility = View.GONE
                } else {
                    if (it.result.equals("Items added successfully", true)) {
                        fromAddItemOrTip = true
                        cardDialog?.dismiss()
                        Util.alertDialog(it.result ?: "", this)
                        callOrderDetailService()
                    } else {
                        cardDialog?.card_progress?.visibility = View.GONE
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
        try {
            if (!data?.offeramount.isNullOrEmpty()) {
                data?.offeramount?.let {
                    discount_taxt.text =
                        "Discount-$${String.format("%.2f", data?.offeramount!!.toFloat())}"
                }
            }
            if (!data.tip2.isNullOrEmpty()) {
                tip_two_text.text =
                    "Tips $${String.format("%.2f", data.tip2!!.toFloat())}"
            } else {
                tip_two_text.text = "Tips_____"
            }
            if (data.subtotal != null) {
                sub_total_text.text = "$${data.subtotal}"
            }
            if (data.total != null) {
                total_tax.text = "Total $${data.total}"
            }
            if (data?.tax != null) {
                tax_text.text = "$${String.format("%.2f", data?.tax!!.toFloat())}"
            }
            if (data?.tip != null) {
                tip_text.text = "$${String.format("%.2f", data?.tip!!.toFloat())}"
            }
            if (!data?.deliverycharge.isNullOrEmpty()) {
                data?.deliverycharge?.let {
                    delivery_fee_text.text =
                        "$${String.format("%.2f", data?.deliverycharge!!.toFloat())}"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
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
                showSnackBar(progress_bar, getString(R.string.internet_connected))
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
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
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
