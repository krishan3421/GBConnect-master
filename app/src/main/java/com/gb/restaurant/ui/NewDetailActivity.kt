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
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.print.PrintHelper
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
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.model.order.Item
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.orderdetail.OrderDetailRequest
import com.gb.restaurant.model.orderdetail.OrderDetailResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.ui.adapter.ItemAdapter
import com.gb.restaurant.ui.adapter.NewDetailAdapter
import com.gb.restaurant.ui.adapter.PrinterListAdapter
import com.gb.restaurant.utils.*
import com.gb.restaurant.viewmodel.TipsViewModel
import com.grabull.session.SessionManager
import kotlinx.android.synthetic.main.add_items_layout.*
import kotlinx.android.synthetic.main.content_new_detail.*
import kotlinx.android.synthetic.main.detail_com_footer.*
import kotlinx.android.synthetic.main.detail_footer.*
import kotlinx.android.synthetic.main.detail_footer.delivery_fee_layout
import kotlinx.android.synthetic.main.detail_footer.delivery_fee_text
import kotlinx.android.synthetic.main.detail_footer.discount_taxt
import kotlinx.android.synthetic.main.detail_footer.sub_total_text
import kotlinx.android.synthetic.main.detail_footer.tax_text
import kotlinx.android.synthetic.main.detail_footer.tip_text
import kotlinx.android.synthetic.main.detail_footer.tip_two_text
import kotlinx.android.synthetic.main.detail_footer.total_tax
import kotlinx.android.synthetic.main.new_detail_activity.*
import kotlinx.android.synthetic.main.order_detail_item.*
import kotlinx.android.synthetic.main.tips_layout.*

class NewDetailActivity : BaseActivity() {

    private lateinit var newDetailAdapter: NewDetailAdapter
    private lateinit var list: MutableList<Item>
    private var data: Data? = null
    private lateinit var viewModel: TipsViewModel
    var rsLoginResponse: RsLoginResponse? = null

    public var mCanvas: Canvas? = null
    public var canvasBitmap: Bitmap? = null
    public var mBitmap: Bitmap? = null

    var sessionManager: SessionManager? = null


    companion object {
        private val TAG: String = NewDetailActivity::class.java.simpleName
        private val CONFIRM_PAGE: Int = 11
        val FROMPAGE: String = "FROMPAGE"
    }

    var fromPage: Int = 0// 0 from completed,new and schedule , 1 from active
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor()
        setContentView(R.layout.new_detail_activity)
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
            attachObserver()
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
            initView()
            callOrderDetailService()
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
            toolbar.title = ""// "${getString(R.string.back)}"//"${rsLoginResponse?.data?.name}"
            title_new_back.setOnClickListener { onBackPressed() }
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimary_one));
            newDetailAdapter = NewDetailAdapter(this, data?.items as MutableList<Item>)
            detail_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@NewDetailActivity)
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
            if (!data?.date2.isNullOrEmpty()) {
                order_time_text.text = "ORDER TIME: ${data?.date2}"
            }
            if (!data?.type.isNullOrEmpty() && data?.type!!.contains("Pickup", true)) {
                address_layout.visibility = View.INVISIBLE
                delivery_fee_layout.visibility = View.GONE
            } else {
                address_layout.visibility = View.VISIBLE
                delivery_fee_layout.visibility = View.VISIBLE
            }
            /*  if(!data?.type.isNullOrEmpty() && !data?.payment.isNullOrEmpty()){ //pending- cash(not paid)
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
                delivery_fee_text.text = "$${data?.deliverycharge}"
            }
            if (!data?.mobile.isNullOrEmpty()) {
                phone_text.text = "Ph: ${data?.mobile}"
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
                // "$${String.format("%.2f",data?.deliverycharge!!.toFloat())}"
                discount_taxt.text = "Discount-$${data?.offeramount}"
            } else {
                discount_taxt.text = "Discount-$0.00"
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
            var intent = Intent(this, ConfirmTimeDialogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(ConfirmTimeDialogActivity.ORDER_ID, data!!.id)
            intent.putExtra(ConfirmTimeDialogActivity.TYPE, data!!.type)
            intent.putExtra(ConfirmTimeDialogActivity.HOLD, data!!.hold)
            startActivityForResult(intent, CONFIRM_PAGE)
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
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CONFIRM_PAGE) {
                finishPage()
            }
        }
    }

    fun printPdf(view: View) {
        //var view =    window.decorView.rootView
        detail_recycler.setDrawingCacheEnabled(true)
        val bitmap: Bitmap = Bitmap.createBitmap(detail_recycler.getDrawingCache())
        PrintHelper(this).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
        }.also { printHelper ->
            // val bitmap = BitmapFactory.decodeResource(resources, R.drawable.p_img24)
            printHelper.printBitmap("detail.png - test print", bitmap)
        }
    }

    private fun finishPage() {
        try {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            onBackPressed()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun showCustomViewDialog(data: Data?, dialogBehavior: DialogBehavior = ModalDialog) {
        val dialog = MaterialDialog(this, dialogBehavior).show {
            this.setCanceledOnTouchOutside(false)
            customView(R.layout.tips_layout, scrollable = true, horizontalPadding = true)
            title_header_text.text = "Add Tips to order # ${data?.id}\n(Customer : ${data?.name}))"
            cancel_image.setOnClickListener {
                this.dismiss()
            }
            add_tips_button.setOnClickListener {
                if (!tips_edit.text.isNullOrEmpty()) {
                    callTipsService(tips_edit.text.toString())
                    this.dismiss()
                } else {
                    showToast("Please add Tips")
                }
            }
        }
    }

    private fun callTipsService(tips: String) {
        try {
            if (Validation.isOnline(this)) {
                var orderTipsRequest = OrderTipsRequest()
                orderTipsRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderTipsRequest.order_id = data?.id!!
                orderTipsRequest.order_tips = tips
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

    private fun addItemsViewDialog(dialogBehavior: DialogBehavior = ModalDialog) {
        var itemList = mutableListOf<com.gb.restaurant.model.Item>()
        val dialog = MaterialDialog(this, dialogBehavior).show {
            this.setCanceledOnTouchOutside(false)
            customView(R.layout.add_items_layout, scrollable = true, horizontalPadding = false)
            var myAdapter = ItemAdapter(this@NewDetailActivity, itemList)
            items_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@NewDetailActivity)
                addItemDecoration(ListPaddingDecorationGray(this@NewDetailActivity, 0, 0))
                adapter = myAdapter
            }
            cancel_image_items.setOnClickListener {
                this.dismiss()
            }

            plus_text.setOnClickListener {
                if (!item_text.text.isNullOrEmpty() && !price_text.text.isNullOrEmpty()) {
                    var item = com.gb.restaurant.model.Item(
                        item_text.text.toString(),
                        price_text.text.toString()
                    )
                    itemList.add(item)
                    println("data>>> ${Util.getStringFromBean(item)}")
                    myAdapter.notifyDataSetChanged()
                    item_text.setText("")
                    price_text.setText("")
                } else {
                    Util.alertDialog("Please add Items", this@NewDetailActivity)
                }
            }

            add_tips_button.setOnClickListener {
                if (itemList.isNotEmpty()) {
                    callAddItemsService(itemList)
                    this.dismiss()
                } else {
                    Util.alertDialog("Please add Items", this@NewDetailActivity)
                }
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

    private fun callAddItemsService(itemList: MutableList<com.gb.restaurant.model.Item>) {
        try {
            if (Validation.isOnline(this)) {
                var addOrderItemRequest = AddOrderItemRequest()
                addOrderItemRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                addOrderItemRequest.order_id = data?.orderid!!
                addOrderItemRequest.itemslist = itemList
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


    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { showToast(it) }
        })
        viewModel.addTipsResponse.observe(this, Observer<OrderTipsResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                    showToast(it.result!!)
                } else {
                    showToast(it.result!!)
                }
            }
        })

        viewModel.addItemsOrderResponse.observe(this, Observer<AddOrderItemResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                    showToast(it.result!!)
                } else {
                    showToast(it.result!!)
                    callOrderDetailService()
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
                    // showToast(it.result!!)
                    MyApp.instance.data = it.data
                    data = MyApp.instance.data
                    //createItmList()
                    newDetailAdapter.addAll(it.data?.items as List<Item>)
                    initView()
                    /* if(data!!.items.isNullOrEmpty()){
                         orders_item_count.text = "Order(0 items)"
                     }else{
                         orders_item_count.text = "Order(${data!!.items!!.size} items)"
                     }*/
                }
            }
        })


    }


    private fun createViewModel(): TipsViewModel =
        ViewModelProviders.of(this).get(TipsViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
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
