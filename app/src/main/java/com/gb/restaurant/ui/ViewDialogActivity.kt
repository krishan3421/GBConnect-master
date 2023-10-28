package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import com.gb.restaurant.databinding.ActivityViewDialogPageBinding
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

class ViewDialogActivity : BaseActivity() {

    private lateinit var viewDialogAdapter: ViewDialogAdapter
    private lateinit var list: MutableList<Item>
    private var data: Data? = null
    private lateinit var viewModel: TipsViewModel
    var rsLoginResponse: RsLoginResponse? = null
    private lateinit var binding: ActivityViewDialogPageBinding
    companion object {
        private val TAG: String = ViewDialogActivity::class.java.simpleName
        val FROMPAGE: String = "FROMPAGE"
    }

    var fromPage: Int = 0// 0 from completed,new and schedule , 1 from active
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_view_dialog_page)
        binding = ActivityViewDialogPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            binding.apply {
                setSupportActionBar(toolbar)
                toolbar.navigationIcon = ContextCompat.getDrawable(this@ViewDialogActivity, R.drawable.ic_back)
                toolbar.setNavigationOnClickListener { onBackPressed() }
                toolbar.title = "${rsLoginResponse?.data?.name}"
            }
           binding.activityViewDialog.apply {
               attachObserver()
               viewDialogAdapter = ViewDialogAdapter(this@ViewDialogActivity, data!!, list)
               viewRecycler.apply {
                   setHasFixedSize(true)
                   layoutManager = LinearLayoutManager(this@ViewDialogActivity)
                   // addItemDecoration(ListPaddingDecoration(this@ViewDialogActivity, 0,0))
                   adapter = viewDialogAdapter
               }

               if (fromPage == 0) {
                   buttonLayout.visibility = View.GONE
               } else {
                   buttonLayout.visibility = View.VISIBLE
               }
               if (!data?.holddate2.isNullOrEmpty()) {
                   if (data!!.type.equals("Delivery", true)) {
                       holdTimeText.text = "Hold Order: Delivery Time: ${data!!.holddate2}"
                   } else {
                       holdTimeText.text = "Hold Order: Pickup Time: ${data!!.holddate2}"
                   }
               } else {
                   holdTimeText.visibility = View.GONE
               }
               if (!data?.date2.isNullOrEmpty()) {
                   orderTimeText.text = "ORDER TIME: ${data!!.date2}"
               }

               if (!data?.type.isNullOrEmpty() && !data?.payment.isNullOrEmpty()) { //pending- cash(not paid)
                   var paymentStatus = ""
                   if (data?.payment!!.contains("pending", true)) {
                       paymentStatus = "NOT PAID"
                       deliveryTypeText.setTextColor(
                           ContextCompat.getColor(
                               this@ViewDialogActivity,
                               R.color.colorAccent
                           )
                       )
                   } else {
                       paymentStatus = "PAID"
                       deliveryTypeText.setTextColor(ContextCompat.getColor(this@ViewDialogActivity, R.color.green))
                   }
                   deliveryTypeText.text = "${data?.type!!.toUpperCase()} $paymentStatus"
               }

               if (!data?.status.isNullOrEmpty()) {
                   status.text = "${data?.status}"
               }

               orderIdText.text = "ORDER # ${data!!.id}"
               callOrderDetailService()
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
            val titleHeaderTax = this.findViewById<TextView>(R.id.title_header_text)
            val cancelImage = this.findViewById<ImageView>(R.id.cancel_image)
            val addTipButton = this.findViewById<Button>(R.id.add_tips_button)
            val tipsEdit = this.findViewById<EditText>(R.id.tips_edit)
            titleHeaderTax.text = "Add Tips to order # ${data?.id}\n(Customer : ${data?.name}))"
            cancelImage.setOnClickListener {
                this.dismiss()
            }
            addTipButton.setOnClickListener {
                if (!tipsEdit.text.isNullOrEmpty()) {
                    callTipsService(tipsEdit.text.toString())
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
            val titleText = this.findViewById<View>(R.id.title_text) as TextView
            val recyclerView = this.findViewById<View>(R.id.items_recycler) as RecyclerView
            val cancelImage = this.findViewById<View>(R.id.cancel_image_items) as ImageView
            val plusText = this.findViewById<View>(R.id.plus_text) as TextView
            val addItemButton = this.findViewById<TextView>(R.id.add_item_button);
            val itemEditText = this.findViewById<View>(R.id.item_text) as EditText
            val priceEditText = this.findViewById<View>(R.id.price_text) as EditText
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@ViewDialogActivity)
                addItemDecoration(ListPaddingDecorationGray(this@ViewDialogActivity, 0, 0))
                adapter = myAdapter
            }
            cancelImage.setOnClickListener {
                this.dismiss()
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
                    Util.alertDialog("Please add Items", this@ViewDialogActivity)
                }
            }

            addItemButton.setOnClickListener {
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
        if (show) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

}
