package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
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
import com.gb.restaurant.ui.adapter.ViewDialogAdapter
import com.gb.restaurant.utils.ListPaddingDecorationGray
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.TipsViewModel
import kotlinx.android.synthetic.main.activity_view_dialog.*
import kotlinx.android.synthetic.main.activity_view_dialog.progress_bar
import kotlinx.android.synthetic.main.activity_view_dialog_page.*
import kotlinx.android.synthetic.main.add_items_layout.*
import kotlinx.android.synthetic.main.tips_layout.*
import kotlinx.android.synthetic.main.tips_layout.add_tips_button

class ViewDialogActivity : BaseActivity() {

    private lateinit var viewDialogAdapter: ViewDialogAdapter
    private lateinit var list: MutableList<Item>
    private var data: Data? = null
    private lateinit var viewModel: TipsViewModel
    var rsLoginResponse: RsLoginResponse? = null

    companion object {
        private val TAG: String = ViewDialogActivity::class.java.simpleName
        val FROMPAGE: String = "FROMPAGE"
    }

    var fromPage: Int = 0// 0 from completed,new and schedule , 1 from active
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_dialog_page)
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
            if (data != null && !data!!.items.isNullOrEmpty()) {
                var itemsList = data!!.items
                itemsList?.forEachIndexed { index, item ->
                    list.add(item!!)
                }
            }
            if (data?.subtotal != null) {
                var item = Item()
                item.heading = "Sub Total"
                item.price = data?.subtotal
                item.isItem = false
                item.isOffer = false
                item.isBold = true
                list.add(item)
            }

            if (!data?.offer.isNullOrEmpty()) {
                var item = Item()
                item.heading = "${data?.offer}"
                item.price = 0.0
                item.isItem = false
                item.isDividerLine = false
                item.isOffer = true
                list.add(item)
            }

            if (!data?.deliverycharge.isNullOrEmpty()) {
                var item = Item()
                item.heading = "Delivery Fee"
                item.price = data?.deliverycharge?.toDouble()
                item.isItem = false
                item.isDividerLine = false
                list.add(item)
            }

            if (data?.tax != null) {
                var item = Item()
                item.heading = "Tax"
                item.price = data?.tax
                item.isItem = false
                item.isDividerLine = false
                list.add(item)
            }

            if (!data?.tip.isNullOrEmpty()) {
                var item = Item()
                item.heading = "Tip"
                item.price = data?.tip?.toDouble()
                item.isItem = false
                //item.isDividerLine =false
                list.add(item)
            }


            if (data?.total != null) {
                var item = Item()
                item.heading = "Total"
                item.price = data?.total
                item.isItem = false
                item.isBold = true
                list.add(item)
            }
            if (!data?.tip2.isNullOrEmpty()) {
                var item = Item()
                item.heading = "Tips"
                item.price = data?.tip2?.toDouble()
                item.isItem = false
                item.isDividerLine = false
                list.add(item)
            }
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
            toolbar.title = "${rsLoginResponse?.data?.name}"
            attachObserver()
            viewDialogAdapter = ViewDialogAdapter(this, data!!, list)
            view_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@ViewDialogActivity)
                // addItemDecoration(ListPaddingDecoration(this@ViewDialogActivity, 0,0))
                adapter = viewDialogAdapter
            }

            if (fromPage == 0) {
                button_layout.visibility = View.GONE
            } else {
                button_layout.visibility = View.VISIBLE
            }
            if (!data?.holddate2.isNullOrEmpty()) {
                if (data!!.type.equals("Delivery", true)) {
                    hold_time_text.text = "Hold Order: Delivery Time: ${data!!.holddate2}"
                } else {
                    hold_time_text.text = "Hold Order: Pickup Time: ${data!!.holddate2}"
                }
            } else {
                hold_time_text.visibility = View.GONE
            }
            if (!data?.date2.isNullOrEmpty()) {
                order_time_text.text = "ORDER TIME: ${data!!.date2}"
            }

            if (!data?.type.isNullOrEmpty() && !data?.payment.isNullOrEmpty()) { //pending- cash(not paid)
                var paymentStatus = ""
                if (data?.payment!!.contains("pending", true)) {
                    paymentStatus = "NOT PAID"
                    delivery_type_text.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.colorAccent
                        )
                    )
                } else {
                    paymentStatus = "PAID"
                    delivery_type_text.setTextColor(ContextCompat.getColor(this, R.color.green))
                }
                delivery_type_text.text = "${data?.type!!.toUpperCase()} $paymentStatus"
            }

            if (!data?.status.isNullOrEmpty()) {
                status.text = "${data?.status}"
            }

            order_id_text.text = "ORDER # ${data!!.id}"
            callOrderDetailService()
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
            var myAdapter = ItemAdapter(this@ViewDialogActivity, itemList)
            items_recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@ViewDialogActivity)
                addItemDecoration(ListPaddingDecorationGray(this@ViewDialogActivity, 0, 0))
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
                    Util.alertDialog("Please add Items", this@ViewDialogActivity)
                }
            }

            add_tips_button.setOnClickListener {
                if (itemList.isNotEmpty()) {
                    callAddItemsService(itemList)
                    this.dismiss()
                } else {
                    Util.alertDialog("Please add Items", this@ViewDialogActivity)
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
                    createItmList()
                    viewDialogAdapter.addAll(list)
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

}
