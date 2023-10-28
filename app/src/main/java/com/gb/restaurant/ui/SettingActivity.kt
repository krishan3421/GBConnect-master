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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
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
import com.gb.restaurant.databinding.ActivitySearchBinding
import com.gb.restaurant.databinding.ActivitySettingBinding
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
import com.gb.restaurant.session.SessionManager
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
    private lateinit var binding: ActivitySettingBinding
    var sessionManager: SessionManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_setting)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            binding.contentSetting.txtCurrentPrinter.text =
                "Current selected Printer: " + printerType + " " + sessionManager!!.getPrinterAddress()
        } else {
            binding.contentSetting.txtCurrentPrinter.text =
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
            setSupportActionBar(binding.toolbar)
            binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
            binding.settingTitle.setOnClickListener { onBackPressed() }
            binding.toolbar.title = ""// getString(R.string.back)//"${rsLoginResponse?.data?.name}"
            supportActionBar?.setDisplayShowTitleEnabled(false)
            binding.contentSetting.minusPickupEstimate.setOnClickListener(this)
            binding.contentSetting.plusPickupEstimate.setOnClickListener(this)

            binding.contentSetting.minusDeliveryEstimate.setOnClickListener(this)
            binding.contentSetting.plusDeliveryEstimate.setOnClickListener(this)

            binding.contentSetting.minusMiles.setOnClickListener(this)
            binding.contentSetting.plusMiles.setOnClickListener(this)

            binding.contentSetting.minusMinDelivery.setOnClickListener(this)
            binding.contentSetting.plusMinDelivery.setOnClickListener(this)

            binding.contentSetting.minusDeliveryCharge.setOnClickListener(this)
            binding.contentSetting.plusDeliveryCharge.setOnClickListener(this)
            //percent_radio.setOnClickListener(this)
            // doller_radio.setOnClickListener(this)
            binding.contentSetting.percentImage.setOnClickListener(this)
            binding.contentSetting.dollerImage.setOnClickListener(this)
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
                    binding.contentSetting.minimumDeliveryTitle.text =
                        "MINIMUM DELIVERY ${rsLoginResponse?.data?.dchargetype}"

                if (!rsLoginResponse?.data?.dchargetype.isNullOrEmpty()) {

                    if (rsLoginResponse?.data?.dchargetype!!.contains("$", true)) {
                        binding.contentSetting.dollerRadio.isChecked = true
                    } else {
                        binding.contentSetting.percentRadio.isChecked = true
                    }
                }
                binding.contentSetting.selfGbDeliveryLayout.gbSwitch.isChecked = rsLoginResponse?.data?.gbdelivery == "Yes"
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

            binding.contentSetting.selfGbDeliveryLayout.gbSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener() { compoundButton: CompoundButton, b: Boolean ->
                gbSelect = if (b) {
                    Constant.GB_DELIVERY.GB
                } else {
                    Constant.GB_DELIVERY.SELF
                }
                submit(binding.contentSetting.selfGbDeliveryLayout.gbSwitch)
            });
            binding.contentSetting.percentRadio.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    binding.contentSetting.dollerRadio.isChecked = false
                    deliveryCharge(deliveryChargeValue, fromButtonClick = true)
                    // submit(percent_radio)
                }
            })
            binding.contentSetting.dollerRadio.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    binding.contentSetting.percentRadio.isChecked = false
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
            binding.contentSetting.apply {
                if (rsLoginResponse?.data?.stoptoday.isNullOrEmpty()) {
                    stopOpenButton.text = getString(R.string.stop_order_today)
                    //stop_open_button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary_one))
                    // stop_open_button.background = getDrawable(R.drawable.romance_color_round_corner)
                    stopOpenButton.background = getDrawable(R.drawable.colorprimary_round_corner)
                    stopOpenButton.setTextColor(ContextCompat.getColor(this@SettingActivity, R.color.white))
                } else {
                    stopOpenButton.text = getString(R.string.open_order_today)
                    stopOpenButton.background = getDrawable(R.drawable.green_round_corner)
                    stopOpenButton.setTextColor(ContextCompat.getColor(this@SettingActivity, R.color.white))
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    override fun onClick(view: View?) {
        binding.contentSetting.apply {
            when (view) {
                minusPickupEstimate -> {
                    if (pickEstimateValue > 0) {
                        pickEstimateValue--
                        pickUpEstimate(pickEstimateValue)
                    }
                }
                plusPickupEstimate -> {
                    if (pickEstimateValue < 500) {
                        pickEstimateValue++
                        pickUpEstimate(pickEstimateValue)
                    }
                }
                minusDeliveryEstimate -> {
                    if (deliveryEstimateValue > 0) {
                        deliveryEstimateValue--
                        deliveryEstimate(deliveryEstimateValue)
                    }
                }
                plusDeliveryEstimate -> {
                    if (deliveryEstimateValue < 500) {
                        deliveryEstimateValue++
                        deliveryEstimate(deliveryEstimateValue)
                    }
                }
                minusMiles -> {
                    if (defaultMilesValue > 0) {
                        defaultMilesValue--
                        defaultMiles(defaultMilesValue)
                    }
                }
                plusMiles -> {
                    if (defaultMilesValue < 500) {
                        defaultMilesValue++
                        defaultMiles(defaultMilesValue)
                    }
                }
                minusMinDelivery -> {
                    if (minDeliveryValue > 0) {
                        minDeliveryValue--
                        minDelivery(minDeliveryValue)
                    }
                }
                plusMinDelivery -> {
                    if (minDeliveryValue < 500) {
                        minDeliveryValue++
                        minDelivery(minDeliveryValue)
                    }
                }
                minusDeliveryCharge -> {
                    if (deliveryChargeValue > 0) {
                        deliveryChargeValue -= 0.5
                        deliveryCharge(deliveryChargeValue)
                    }
                }
                plusDeliveryCharge -> {
                    if (deliveryChargeValue < 500) {
                        deliveryChargeValue += 0.5
                        deliveryCharge(deliveryChargeValue)
                    }
                }
                percentImage -> {
                    chargeTypePopUp("%")
                }
                dollerImage -> {
                    chargeTypePopUp("$")
                }
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

    private fun pickUpEstimate(pickUpEstimat: Int, fromButtonClick: Boolean = true) {
        try {
            binding.contentSetting.apply {
                pickupEstimate.text = "$pickUpEstimat Minutes"
                if (fromButtonClick) {
                    pickupSaveButton.background = getDrawable(R.drawable.white_blue_round_corner)
                    pickupSaveButton.setTextColor(ContextCompat.getColor(this@SettingActivity, R.color.blue_color))
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }


    private fun deliveryEstimate(deliveryEstimat: Int, fromButtonClick: Boolean = true) {
        try {
            binding.contentSetting.apply {
                deliveryEstimate.text = "$deliveryEstimat Minutes"
                if (fromButtonClick) {
                    deliverySaveButton.background = getDrawable(R.drawable.white_blue_round_corner)
                    deliverySaveButton.setTextColor(ContextCompat.getColor(this@SettingActivity, R.color.blue_color))
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun defaultMiles(defaultMiles: Int, fromButtonClick: Boolean = true) {
        try {
            binding.contentSetting.apply {
                miles.text = "$defaultMiles Miles"
                if (fromButtonClick) {
                    defaultSaveButton.background = getDrawable(R.drawable.white_blue_round_corner)
                    defaultSaveButton.setTextColor(ContextCompat.getColor(this@SettingActivity, R.color.blue_color))
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun minDelivery(minDeliver: Int, fromButtonClick: Boolean = true) {
        try {
            binding.contentSetting.apply {
                minDelivery.text = "$minDeliver"
                if (fromButtonClick) {
                    minDeliverySaveButton.background =
                        getDrawable(R.drawable.white_blue_round_corner)
                    minDeliverySaveButton.setTextColor(
                        ContextCompat.getColor(
                            this@SettingActivity,
                            R.color.blue_color
                        )
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun deliveryCharge(deliveryCharg: Double, fromButtonClick: Boolean = true) {
        try {
            binding.contentSetting.apply {
                deliveryCharge.text = "$deliveryCharg"
                if (fromButtonClick) {
                    deliveryChargeSaveButton.background =
                        getDrawable(R.drawable.white_blue_round_corner)
                    deliveryChargeSaveButton.setTextColor(
                        ContextCompat.getColor(
                            this@SettingActivity,
                            R.color.blue_color
                        )
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun stopOrderToday(view: View) {
        try {
            if (binding.contentSetting.stopOpenButton.text.toString().contains("STOP")) {
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
                selectPrinter(binding.contentSetting.btnSelectPrinter)
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
            binding.contentSetting.selfGbDeliveryLayout.gbSwitch.isChecked = false
            gbSelect = Constant.GB_DELIVERY.SELF
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun gbDelivery(view: View) {
        try {
            binding.contentSetting.selfGbDeliveryLayout.gbSwitch.isChecked = true
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
            val desText = this.findViewById<TextView>(R.id.description_text)
            val closeDialog = this.findViewById<ImageView>(R.id.close_dialog)
            desText.text = rsLoginResponse?.data?.terms
            closeDialog.setOnClickListener {
                this.dismiss()
            }
        }
    }

    fun submit(view: View) {
        binding.contentSetting.apply {
            try {
                when (view) {
                    pickupSaveButton -> {
                        //println("button>>>> ${pickup_save_button.background.constantState}")
                        //println("drawable>>>> ${getDrawable(R.drawable.white_darkgray_round_corner)?.constantState}")
                        if (pickupSaveButton.background.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                            return
                        pickupSaveButton.background =
                            getDrawable(R.drawable.white_darkgray_round_corner)
                        pickupSaveButton.setTextColor(ContextCompat.getColor(this@SettingActivity, R.color.dark_gray))
                    }
                    deliverySaveButton -> {
                        if (deliverySaveButton.background.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                            return
                        deliverySaveButton.background =
                            getDrawable(R.drawable.white_darkgray_round_corner)
                        deliverySaveButton.setTextColor(
                            ContextCompat.getColor(
                                this@SettingActivity,
                                R.color.dark_gray
                            )
                        )
                    }
                    defaultSaveButton -> {
                        if (defaultSaveButton.background.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                            return
                        defaultSaveButton.background =
                            getDrawable(R.drawable.white_darkgray_round_corner)
                        defaultSaveButton.setTextColor(
                            ContextCompat.getColor(
                                this@SettingActivity,
                                R.color.dark_gray
                            )
                        )
                    }
                    minDeliverySaveButton -> {
                        if (minDeliverySaveButton.background?.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                            return
                        minDeliverySaveButton.background =
                            getDrawable(R.drawable.white_darkgray_round_corner)
                        minDeliverySaveButton.setTextColor(
                            ContextCompat.getColor(
                                this@SettingActivity,
                                R.color.dark_gray
                            )
                        )
                    }
                    deliveryChargeSaveButton -> {
                        if (deliveryChargeSaveButton.background?.constantState == getDrawable(R.drawable.white_darkgray_round_corner)?.constantState)
                            return
                        deliveryChargeSaveButton.background =
                            getDrawable(R.drawable.white_darkgray_round_corner)
                        deliveryChargeSaveButton.setTextColor(
                            ContextCompat.getColor(
                                this@SettingActivity,
                                R.color.dark_gray
                            )
                        )
                    }
                }
                var updateSettingRequest = UpdateSettingRequest()
                updateSettingRequest.deviceversion = Util.getVersionName(this@SettingActivity)
                updateSettingRequest.pickup = pickEstimateValue.toString()
                updateSettingRequest.delivery = deliveryEstimateValue.toString()
                updateSettingRequest.miles = defaultMilesValue.toString()
                updateSettingRequest.mindelivery = minDeliveryValue.toString()
                updateSettingRequest.deliverycharge = deliveryChargeValue.toString()
                when {
                    dollerRadio.isChecked -> updateSettingRequest.deliverychargetype = "$"
                    percentRadio.isChecked -> updateSettingRequest.deliverychargetype = "%"
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
    }

    private fun callUpdateSetting(updateSettingRequest: UpdateSettingRequest) {
        try {
            if (Validation.isOnline(this)) {
                viewModel.updateSetting(updateSettingRequest)
            } else {
                showSnackBar(binding.progressBar, getString(R.string.internet_connected))
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
                if (binding.contentSetting.stopOpenButton.text.toString()
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
                showSnackBar(binding.progressBar, getString(R.string.internet_connected))
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
            it?.let { showSnackBar(binding.progressBar, it) }
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
            if (binding.contentSetting.dollerRadio.isChecked) {
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
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
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
            binding.contentSetting.txtCurrentPrinter.text =
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
                                        if (ActivityCompat.checkSelfPermission(
                                                this@SettingActivity,
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


}
