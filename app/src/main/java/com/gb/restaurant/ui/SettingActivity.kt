package com.gb.restaurant.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
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
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.PrinterModel
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.model.stoporder.StopOrderRequest
import com.gb.restaurant.model.stoporder.StopOrderResponse
import com.gb.restaurant.model.updatesetting.UpdateSettingRequest
import com.gb.restaurant.model.updatesetting.UpdateSettingResponse
import com.gb.restaurant.ui.adapter.PrinterListAdapter
import com.gb.restaurant.utils.BluetoothDiscovery
import com.gb.restaurant.utils.IpScanner
import com.gb.restaurant.utils.Util
import com.gb.restaurant.utils.Utils
import com.gb.restaurant.viewmodel.DestinViewModel
import com.grabull.session.SessionManager

import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.content_setting.*
import kotlinx.android.synthetic.main.self_gb_delivery_layout.*
import kotlinx.android.synthetic.main.term_layout.*
import java.util.*

class SettingActivity : BaseActivity(), View.OnClickListener {

    var rsLoginResponse: RsLoginResponse? = null

    companion object {
        private val TAG: String = SettingActivity::class.java.simpleName
    }

    private var pickEstimateValue = 0
    private var deliveryEstimateValue = 0
    private var defaultMilesValue = 0
    private var minDeliveryValue = 0
    private var deliveryChargeValue = 0.0
    private var gbSelect: String = Constant.GB_DELIVERY.SELF
    private lateinit var viewModel: DestinViewModel

    var sessionManager: SessionManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        sessionManager = SessionManager(this)

        initData()
        initView()

        if (sessionManager!!.getPrinterAddress().isNotEmpty()) {
            var printerType = ""
            if (sessionManager!!.getPrinterType() == 1) {
                printerType = "Bluetooth"
            } else if (sessionManager!!.getPrinterType() == 2) {
                printerType = "LAN"
            }
            txtCurrentPrinter.text =
                "Current selected Printer: " + printerType + " " + sessionManager!!.getPrinterAddress()
        } else {
            txtCurrentPrinter.text =
                "Current selected Printer: None"
        }
    }

    private fun initData() {
        try {
            rsLoginResponse = MyApp.instance.rsLoginResponse
            viewModel = createViewModel()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun initView() {
        try {
            setSupportActionBar(toolbar)
            toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            setting_title.setOnClickListener { onBackPressed() }
            toolbar.title = ""// getString(R.string.back)//"${rsLoginResponse?.data?.name}"
            minus_pickup_estimate.setOnClickListener(this)
            plus_pickup_estimate.setOnClickListener(this)

            minus_delivery_estimate.setOnClickListener(this)
            plus_delivery_estimate.setOnClickListener(this)

            minus_miles.setOnClickListener(this)
            plus_miles.setOnClickListener(this)

            minus_min_delivery.setOnClickListener(this)
            plus_min_delivery.setOnClickListener(this)

            minus_delivery_charge.setOnClickListener(this)
            plus_delivery_charge.setOnClickListener(this)
            //percent_radio.setOnClickListener(this)
            // doller_radio.setOnClickListener(this)
            percent_image.setOnClickListener(this)
            doller_image.setOnClickListener(this)
            attachObserver()
            if (rsLoginResponse != null && rsLoginResponse!!.data != null) {
                if (!rsLoginResponse?.data?.pickup.isNullOrEmpty())
                    pickEstimateValue = rsLoginResponse?.data?.pickup!!.toInt()

                if (!rsLoginResponse?.data?.delivery.isNullOrEmpty())
                    deliveryEstimateValue = rsLoginResponse?.data?.delivery!!.toInt()

                if (!rsLoginResponse?.data?.miles.isNullOrEmpty())
                    defaultMilesValue = rsLoginResponse?.data?.miles!!.toInt()

                if (!rsLoginResponse?.data?.mindelivery.isNullOrEmpty())
                    minDeliveryValue = rsLoginResponse?.data?.mindelivery!!.toInt()

                if (!rsLoginResponse?.data?.dcharge.isNullOrEmpty())
                    deliveryChargeValue = rsLoginResponse?.data?.dcharge!!.toDouble()

                if (!rsLoginResponse?.data?.dchargetype.isNullOrEmpty())
                    minimum_delivery_title.text =
                        "MINIMUM DELIVERY ${rsLoginResponse?.data?.dchargetype}"

                if (!rsLoginResponse?.data?.dchargetype.isNullOrEmpty()) {

                    if (rsLoginResponse?.data?.dchargetype!!.contains("$", true)) {
                        doller_radio.isChecked = true
                    } else {
                        percent_radio.isChecked = true
                    }
                }
                gb_switch.isChecked = rsLoginResponse?.data?.gbdelivery == "Yes"
                gbSelect = if (!rsLoginResponse?.data?.gbdelivery.isNullOrEmpty()) {
                    Constant.GB_DELIVERY.GB
                } else {
                    Constant.GB_DELIVERY.SELF
                }

                stopOpenButtonText()
            }
            pickUpEstimate(pickEstimateValue, fromButtonClick = false)
            deliveryEstimate(deliveryEstimateValue, fromButtonClick = false)
            defaultMiles(defaultMilesValue, fromButtonClick = false)
            minDelivery(minDeliveryValue, fromButtonClick = false)
            deliveryCharge(deliveryChargeValue, fromButtonClick = false)

            gb_switch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener() { compoundButton: CompoundButton, b: Boolean ->
                gbSelect = if (b) {
                    Constant.GB_DELIVERY.GB
                } else {
                    Constant.GB_DELIVERY.SELF
                }
                submit(gb_switch)
            });
            percent_radio.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    doller_radio.isChecked = false
                    deliveryCharge(deliveryChargeValue, fromButtonClick = true)
                    // submit(percent_radio)
                }
            })
            doller_radio.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    percent_radio.isChecked = false
                    deliveryCharge(deliveryChargeValue, fromButtonClick = true)
                    //submit(doller_radio)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun stopOpenButtonText() {
        try {
            if (rsLoginResponse?.data?.stoptoday.isNullOrEmpty()) {
                stop_open_button.text = getString(R.string.stop_order_today)
                //stop_open_button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary_one))
                // stop_open_button.background = getDrawable(R.drawable.romance_color_round_corner)
                stop_open_button.background = getDrawable(R.drawable.colorprimary_round_corner)
                stop_open_button.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                stop_open_button.text = getString(R.string.open_order_today)
                stop_open_button.background = getDrawable(R.drawable.green_round_corner)
                stop_open_button.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            minus_pickup_estimate -> {
                if (pickEstimateValue > 0) {
                    pickEstimateValue--
                    pickUpEstimate(pickEstimateValue)
                }
            }
            plus_pickup_estimate -> {
                if (pickEstimateValue < 500) {
                    pickEstimateValue++
                    pickUpEstimate(pickEstimateValue)
                }
            }
            minus_delivery_estimate -> {
                if (deliveryEstimateValue > 0) {
                    deliveryEstimateValue--
                    deliveryEstimate(deliveryEstimateValue)
                }
            }
            plus_delivery_estimate -> {
                if (deliveryEstimateValue < 500) {
                    deliveryEstimateValue++
                    deliveryEstimate(deliveryEstimateValue)
                }
            }
            minus_miles -> {
                if (defaultMilesValue > 0) {
                    defaultMilesValue--
                    defaultMiles(defaultMilesValue)
                }
            }
            plus_miles -> {
                if (defaultMilesValue < 500) {
                    defaultMilesValue++
                    defaultMiles(defaultMilesValue)
                }
            }
            minus_min_delivery -> {
                if (minDeliveryValue > 0) {
                    minDeliveryValue--
                    minDelivery(minDeliveryValue)
                }
            }
            plus_min_delivery -> {
                if (minDeliveryValue < 500) {
                    minDeliveryValue++
                    minDelivery(minDeliveryValue)
                }
            }
            minus_delivery_charge -> {
                if (deliveryChargeValue > 0) {
                    deliveryChargeValue -= 0.5
                    deliveryCharge(deliveryChargeValue)
                }
            }
            plus_delivery_charge -> {
                if (deliveryChargeValue < 500) {
                    deliveryChargeValue += 0.5
                    deliveryCharge(deliveryChargeValue)
                }
            }
            percent_image -> {
                chargeTypePopUp("%")
            }
            doller_image -> {
                chargeTypePopUp("$")
            }
        }
    }

    private fun chargeTypePopUp(type: String) {
        try {
            MaterialDialog(this).show {
                message(
                    null,
                    "If you want to have Delivery charges equal to $type value of order Subtotal."
                )

            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun pickUpEstimate(pickUpEstimate: Int, fromButtonClick: Boolean = true) {
        try {
            pickup_estimate.text = "$pickUpEstimate Minutes"
            if (fromButtonClick) {
                pickup_save_button.background = getDrawable(R.drawable.white_blue_round_corner)
                pickup_save_button.setTextColor(ContextCompat.getColor(this, R.color.blue_color))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }


    private fun deliveryEstimate(deliveryEstimate: Int, fromButtonClick: Boolean = true) {
        try {
            delivery_estimate.text = "$deliveryEstimate Minutes"
            if (fromButtonClick) {
                delivery_save_button.background = getDrawable(R.drawable.white_blue_round_corner)
                delivery_save_button.setTextColor(ContextCompat.getColor(this, R.color.blue_color))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun defaultMiles(defaultMiles: Int, fromButtonClick: Boolean = true) {
        try {
            miles.text = "$defaultMiles Miles"
            if (fromButtonClick) {
                default_save_button.background = getDrawable(R.drawable.white_blue_round_corner)
                default_save_button.setTextColor(ContextCompat.getColor(this, R.color.blue_color))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun minDelivery(minDelivery: Int, fromButtonClick: Boolean = true) {
        try {
            min_delivery.text = "$minDelivery"
            if (fromButtonClick) {
                min_delivery_save_button.background =
                    getDrawable(R.drawable.white_blue_round_corner)
                min_delivery_save_button.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blue_color
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun deliveryCharge(deliveryCharge: Double, fromButtonClick: Boolean = true) {
        try {
            delivery_charge.text = "$deliveryCharge"
            if (fromButtonClick) {
                delivery_charge_save_button.background =
                    getDrawable(R.drawable.white_blue_round_corner)
                delivery_charge_save_button.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blue_color
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun stopOrderToday(view: View) {
        try {
            if (stop_open_button.text.toString().contains("STOP")) {
                openStopOpenPopUp("Stop")
            } else {
                openStopOpenPopUp("Open")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }


    fun selectPrinter(view: View) {


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
                showDialog(this)
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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPrinter(btnSelectPrinter)
            }
        }


    }


    private fun openStopOpenPopUp(type: String) {
        try {
            MaterialDialog(this).show {
                title(R.string.grabull_lower)
                message(null, "Order $type for ${Util.getMMM_DD_YYYY(Date())}")
                positiveButton {
                    callService()
                }
                positiveButton(R.string.ok)
                negativeButton(R.string.no)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun openTermDialog(view: View) {
        try {
            showCustomViewDialog()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun selfDelivery(view: View) {
        try {
            gb_switch.isChecked = false
            gbSelect = Constant.GB_DELIVERY.SELF
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun gbDelivery(view: View) {
        try {
            gb_switch.isChecked = true
            gbSelect = Constant.GB_DELIVERY.GB
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun storeProfileMethod(view: View) {
        try {

            rsLoginResponse?.data?.pickup
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }


    private fun showCustomViewDialog(dialogBehavior: DialogBehavior = ModalDialog) {
        val dialog = MaterialDialog(this, dialogBehavior).show {
            this.cancelOnTouchOutside(false)
            customView(R.layout.term_layout, scrollable = true, horizontalPadding = true)
            description_text.text = rsLoginResponse?.data?.terms
            close_dialog.setOnClickListener {
                this.dismiss()
            }
        }
    }

    fun submit(view: View) {
        try {
            when (view) {
                pickup_save_button -> {
                    //println("button>>>> ${pickup_save_button.background.constantState}")
                    //println("drawable>>>> ${getDrawable(R.drawable.white_darkgray_round_corner)?.constantState}")
                    if (pickup_save_button.background.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                        return
                    pickup_save_button.background =
                        getDrawable(R.drawable.white_darkgray_round_corner)
                    pickup_save_button.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
                }
                delivery_save_button -> {
                    if (delivery_save_button.background.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                        return
                    delivery_save_button.background =
                        getDrawable(R.drawable.white_darkgray_round_corner)
                    delivery_save_button.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.dark_gray
                        )
                    )
                }
                default_save_button -> {
                    if (default_save_button.background.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                        return
                    default_save_button.background =
                        getDrawable(R.drawable.white_darkgray_round_corner)
                    default_save_button.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.dark_gray
                        )
                    )
                }
                min_delivery_save_button -> {
                    if (min_delivery_save_button.background?.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                        return
                    min_delivery_save_button.background =
                        getDrawable(R.drawable.white_darkgray_round_corner)
                    min_delivery_save_button.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.dark_gray
                        )
                    )
                }
                delivery_charge_save_button -> {
                    if (delivery_charge_save_button.background?.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                        return
                    delivery_charge_save_button.background =
                        getDrawable(R.drawable.white_darkgray_round_corner)
                    delivery_charge_save_button.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.dark_gray
                        )
                    )
                }
            }
            var updateSettingRequest = UpdateSettingRequest()
            updateSettingRequest.deviceversion = Util.getVersionName(this)
            updateSettingRequest.pickup = pickEstimateValue.toString()
            updateSettingRequest.delivery = deliveryEstimateValue.toString()
            updateSettingRequest.miles = defaultMilesValue.toString()
            updateSettingRequest.mindelivery = minDeliveryValue.toString()
            updateSettingRequest.deliverycharge = deliveryChargeValue.toString()
            when {
                doller_radio.isChecked -> updateSettingRequest.deliverychargetype = "$"
                percent_radio.isChecked -> updateSettingRequest.deliverychargetype = "%"
                else -> updateSettingRequest.deliverychargetype = "$"
            }
            updateSettingRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
            updateSettingRequest.gbdelivery = gbSelect
            println("update request>>>>>> ${Util.getStringFromBean(updateSettingRequest)}")
            callUpdateSetting(updateSettingRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun callUpdateSetting(updateSettingRequest: UpdateSettingRequest) {
        try {
            if (Validation.isOnline(this)) {
                viewModel.updateSetting(updateSettingRequest)
            } else {
                showSnackBar(progress_bar, getString(R.string.internet_connected))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun callService() {
        try {
            if (Validation.isOnline(this)) {
                var stopOrderRequest = StopOrderRequest()
                stopOrderRequest.deviceversion = Util.getVersionName(this)
                stopOrderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                if (stop_open_button.text.toString()
                        .contains(getString(R.string.stop_order_today), true)
                ) {
                    stopOrderRequest.service_type = Constant.SERVICE_TYPE.GET_STOP_TODAY
                    println("stop servicetype>>>>>> }")
                } else {
                    println("open servicetype>>>>>> }")
                    stopOrderRequest.service_type = Constant.SERVICE_TYPE.GET_OPEN_TODAY
                }
                println("stop request>>>>>> ${Util.getStringFromBean(stopOrderRequest)}")
                viewModel.stopOrderToday(stopOrderRequest)
            } else {
                showSnackBar(progress_bar, getString(R.string.internet_connected))
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
            it?.let { showSnackBar(progress_bar, it) }
        })
        viewModel.stopOrderResponse.observe(this, Observer<StopOrderResponse> {
            it?.let {

                if (it.status == Constant.STATUS.FAIL) {
                    showToast(it.result!!)
                } else {
                    showToast(it.result!!)
                    if (it.data?.message?.contains("Order Stopped Today", true)!!) {
                        rsLoginResponse!!.data!!.stoptoday = Util.getYYYYMMDD()
                    } else {
                        rsLoginResponse!!.data!!.stoptoday = ""
                    }
                    MyApp.instance.rsLoginResponse = rsLoginResponse
                    stopOpenButtonText()
                }
            }
        })

        viewModel.updateSettingResponse.observe(this, Observer<UpdateSettingResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                    showToast(it.result!!)
                } else {
                    showToast(it.result!!)
                    setDataAfterUpdateSuccess()
                }
            }
        })


    }

    private fun setDataAfterUpdateSuccess() {
        try {
            rsLoginResponse?.data?.pickup = "$pickEstimateValue"
            rsLoginResponse?.data?.delivery = "$deliveryEstimateValue"
            rsLoginResponse?.data?.miles = "$defaultMilesValue"
            rsLoginResponse?.data?.mindelivery = "$minDeliveryValue"
            rsLoginResponse?.data?.dcharge = "$deliveryChargeValue"
            rsLoginResponse?.data?.dchargetype = "$deliveryChargeValue"
            var pickStartTime = pickEstimateValue
            var deliveryStartTime = deliveryEstimateValue
            var pickUpList = mutableListOf<Int>()
            var deliveryList = mutableListOf<Int>()
            var totalPickUpLength = pickStartTime + 5 * 6
            var totalDeliveryLength = deliveryStartTime + 5 * 6
            for (i in pickStartTime until totalPickUpLength step 5) {
                pickUpList.add(i)
            }
            for (j in deliveryStartTime until totalDeliveryLength step 5) {
                deliveryList.add(j)
            }
            rsLoginResponse?.data?.pickuptime = pickUpList
            rsLoginResponse?.data?.deliverytime = deliveryList
            //println("pickUpList>>> ${pickUpList}")
            //println("deliveryList>>> ${deliveryList}")
            if (gbSelect == Constant.GB_DELIVERY.GB) {
                rsLoginResponse?.data?.gbdelivery = "Yes"
            } else {
                rsLoginResponse?.data?.gbdelivery = "No"
            }
            if (doller_radio.isChecked) {
                rsLoginResponse?.data?.dchargetype = "$"
            } else {
                rsLoginResponse?.data?.dchargetype = "%"
            }
            MyApp.instance.rsLoginResponse = rsLoginResponse
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun createViewModel(): DestinViewModel =
        ViewModelProviders.of(this).get(DestinViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar.visibility = View.VISIBLE else progress_bar.visibility = View.GONE
    }

    private fun showDialog(mContext: Context) {

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

            if (bluetoothDiscovery != null) {
                bluetoothDiscovery.stopDiscovering()
                bluetoothDiscovery.unregisterReceiver()
            }

            sessionManager!!.setPrinterAddress(printerList.printer_id)
            sessionManager!!.setPrinterType(printerList.printer_type)


            var printerType = ""
            if (printerList.printer_type == 1) {
                printerType = "Bluetooth"
            } else if (printerList.printer_type == 2) {
                printerType = "LAN"
            }
            txtCurrentPrinter.text =
                "Current selected Printer: " + printerType + " " + printerList.printer_id

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

                            override fun onBluetoothDeviceFound(mDeviceList: ArrayList<BluetoothDevice?>) {
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


}
