package com.gb.restaurant.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.gb.restaurant.CATPrintSDK.Canvas
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.databinding.ActivityOrdersBinding
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.confirmorder.OrderStatusRequest
import com.gb.restaurant.model.confirmorder.OrderStatusResponse
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.model.order.OrderRequest
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.push.MyFirebaseMessagingService
import com.gb.restaurant.push.PushMessage
import com.gb.restaurant.push.TYPE
import com.gb.restaurant.ui.fragments.*
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.OrderViewModel
import com.gb.restaurant.session.SessionManager
import com.gb.restaurant.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

//https://stackoverflow.com/questions/17685787/access-a-method-of-a-fragment-from-the-viewpager-activity
class OrdersActivity : BaseActivity(), ViewPager.OnPageChangeListener,
    ActiveFragment.OnFragmentInteractionListener,
    CompletedFragment.OnFragmentInteractionListener, NewFragment.OnFragmentInteractionListener,
    ScheduledFragment.OnFragmentInteractionListener,
    NewFragment.OnStopListener, ScheduledFragment.OnStopListener, ActiveFragment.OnStopListener,
    NewFragment.OnReservListener {

    private lateinit var samplePagerAdapter: SamplePagerAdapter
    lateinit var mediaPlayer: MediaPlayer
    lateinit var tabNew: View
    lateinit var tabNewText: TextView
    lateinit var tabSchedule: View
    var newCount = 0
    var scheduleCount = 0
    var rsLoginResponse: RsLoginResponse? = null
    lateinit var orderViewPager: ViewPager
    private lateinit var viewModel: OrderViewModel
    var sessionManager: SessionManager? = null
    lateinit var mainHandler: Handler
    private lateinit var binding: ActivityOrdersBinding
    var newOrderList :List<Data?> = emptyList()
    private var mCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    private var mBitmap: Bitmap? = null
    var confirmData:Data?=null
    var handler = Handler()
    companion object {
        private val TAG: String = OrdersActivity::class.java.simpleName
        var isPageVisible: Boolean = false
        private val RESERVATION: Int = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
       // setContentView(R.layout.activity_orders)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setSupportActionBar(toolbar)
        initView()
    }

    private fun initView() {
        try {
            sessionManager = SessionManager(this)
            viewModel = createViewModel()
            attachObserver()
            rsLoginResponse = MyApp.instance.rsLoginResponse
            mainHandler = Handler(Looper.getMainLooper())
            /* toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
             toolbar.title = getString(R.string.back)
             toolbar.setNavigationOnClickListener { onBackPressed() }
             toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.colorAccent))*/
            binding.customAppbar.backLayout.setOnClickListener {
                onBackPressed()
            }

            binding.customAppbar.backLayout.visibility = View.INVISIBLE
            samplePagerAdapter = SamplePagerAdapter(supportFragmentManager)
            binding.contentOrders.orderViewPager.adapter = samplePagerAdapter
            binding.contentOrders.orderTabLayout.setupWithViewPager(binding.contentOrders.orderViewPager)
            binding.contentOrders.orderViewPager.addOnPageChangeListener(this)
            orderViewPager = binding.contentOrders.orderViewPager
            tabNew = (binding.contentOrders.orderTabLayout.getChildAt(0) as ViewGroup).getChildAt(Constant.TAB.NEW)
//             tabNewText =
//                order_tab_layout.getChildAt(0).findViewById(id.title) as TextView
//             tabNewText.setTextColor(ContextCompat.getColor(this,R.color.colorAccent))
            //tabSchedule = (order_tab_layout.getChildAt(0) as ViewGroup).getChildAt(Constant.TAB.SCHEDULE)

            for (i in 0 until binding.contentOrders.orderTabLayout.tabCount) {
                val tab = (binding.contentOrders.orderTabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                val p = tab.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(0, 0, 10, 0)
                tab.requestLayout()
            }
            mediaPlayer = MediaPlayer.create(this, R.raw.sound);
            mediaPlayer.isLooping = true
            //blinkTab(1)
           // handler.post(runnableCode);
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun callNewOrderService() {
        try {
            if (Validation.isOnline(this)) {
                // fragmentBaseActivity.showToast("broadcast new")
                val orderRequest = OrderRequest()
                orderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId?:""
                orderRequest.service_type =
                    Constant.SERVICE_TYPE.GET_NEW_ORDER//Constant.SERVICE_TYPE.GET_NEW_ORDER
                orderRequest.deviceversion = Util.getVersionName(this)
                // println("new request>>>>> ${Util.getStringFromBean(orderRequest)}")
                viewModel.getOrderResponse(orderRequest, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(BaseFragment.TAG, e.message?:"")
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            while (isActive) {
//                val currentTime = System.currentTimeMillis()
//                val lastEventTime = sessionManager?.getLastPrintTime()?:System.currentTimeMillis()
//                val diffMinutes = (currentTime - lastEventTime) / (60 * 1000)
//                if (diffMinutes >= 10) {
//                    sessionManager?.setPrintTime(currentTime)
//                    Log.d("Coroutine", "Called on main thread")
//                    callService(isPrintLastOrder = true)
//                }
                Log.d("Coroutine", "Called on main thread")
                callService(isPrintLastOrder = true)
                // Repeat every 10 minutes
               // delay(10 * 60 * 1000L)
                delay(10 * 60 * 1000L)
            }
        }
    }

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            println("call>>>>>>>>>>handler")
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread")
            // Repeat this the same runnable code block again another 2 seconds
            // 'this' is referencing the Runnable object
            callService(isPrintLastOrder = true)
            handler.postDelayed(this, 10 * 60 * 1000)
        }
    }
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("call>>>>>>>>>>>>>")
            //do stuffs
            refreshPage(tabNew)
        }
    }

    fun backMethod(view: View) {
        try {
            onBackPressed()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"")
        }
    }

    private fun statusBarTransparent() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        if (Build.VERSION.SDK_INT in 19..20) {
//            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
//        }
//        if (Build.VERSION.SDK_INT >= 19) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        }
//        if (Build.VERSION.SDK_INT >= 21) {
//            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
//            window.statusBarColor = Color.TRANSPARENT
//        }
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

    fun backClick(v: View) {
        onBackPressed()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        isPageVisible = true
        LocalBroadcastManager.getInstance(this).registerReceiver(
            pushBroadcastReceiver,
            IntentFilter(MyFirebaseMessagingService.PUSHBROADCAST)
        )

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            registerReceiver(printStatusBroadcast,
                IntentFilter("com.gb.restaurant.utils.returnPrintStatus"),
                RECEIVER_EXPORTED
            );
        }else{
            registerReceiver(printStatusBroadcast,
                IntentFilter("com.gb.restaurant.utils.returnPrintStatus")
            );
        }
        Utils.setBluetooth(true,MyApp.instance)
        //callService(false)
       // mainHandler.post(updateTextTask)

    }
    override fun onPause() {
        super.onPause()
        isPageVisible = false
        unregisterReceiver(printStatusBroadcast);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver)
       // mainHandler.removeCallbacks(updateTextTask)
    }


    fun refreshActiveFragment() {
        ActiveFragment.getInstance()?.callService()
    }

    inner class SamplePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return if (position === 0) {
                NewFragment()
            } /*else if(position === 1){
                ScheduledFragment()
            }*/ else if (position === 1) {
                ActiveFragment()
            } else {
                CompletedFragment()
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            var title: String? = null
            when (position) {
                0 -> {
                    title = "NEW"
                } /*else if (position == 1) {
                        title = "SCHEDULED"
                    }*/
                1 -> {
                    title = "In House"//Active
                }
                2 -> {
                    title = "Completed"
                }
            }
            return title
        }

        override fun getItemPosition(`object`: Any): Int {
            // POSITION_NONE makes it possible to reload the PagerAdapter
            return PagerAdapter.POSITION_NONE
        }
    }

    override fun onFragmentInteraction(position: Int, count: Int) {
        try {
            when (position) {
                Constant.TAB.NEW -> {
                    binding.contentOrders.orderTabLayout.getTabAt(position)!!.text = "NEW($count)";
                    blinkTab(tabNew, Constant.TAB.NEW)
                }
                Constant.TAB.ACTIVE -> {
                    binding.contentOrders.orderTabLayout.getTabAt(position)!!.text = "In House($count)";
                    //blinkTab(tabSchedule,Constant.TAB.SCHEDULE)
                }
                else -> {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"")
        }
    }

    override fun callApiOnRefresh(isPrint: Boolean) {
        callService(isPrint)
    }

    private fun blinkTab(tab: View, position: Int) {
        try {
            if (tab != null) {
                tab.setBackgroundColor(Color.RED)
                // tabNewText.setTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                val anim = AlphaAnimation(0.0f, 1.0f)
                anim.duration = 300 //You can manage the blinking time with this parameter
                anim.startOffset = 20
                anim.repeatMode = Animation.REVERSE
                anim.repeatCount = Animation.INFINITE
                tab.startAnimation(anim)
            } else {
                println("tab $position is null>>>>>>>>>>")
            }
            startMedia()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"")
        }
    }

    fun refreshPage(v: View) {
        try {
            val aniRotate: Animation =
                AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anti_clockwise)
            binding.contentOrders.refreshImage.startAnimation(aniRotate)
            binding.contentOrders.orderViewPager.adapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.order, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                binding.contentOrders.orderViewPager.adapter?.notifyDataSetChanged()
            }
            R.id.action_search -> {
                searchPage()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun reservationPage(view: View) {
        try {
            var intent = Intent(applicationContext, ReservationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivityForResult(intent, RESERVATION)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"")
        }
    }

    fun searchPage() {
        try {
            var intent = Intent(applicationContext, SearchActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"")
        }
    }

    fun eventEnquieryPage(view: View) {
        try {
            var intent = Intent(applicationContext, EventEnquiryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"")
        }
    }

    fun startMedia() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying && MyApp.instance.isAlarm) {
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"")
        }
    }

    private fun stopMedia() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = MediaPlayer.create(this, R.raw.sound);
                    mediaPlayer.isLooping = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"")
        }
    }

    override fun onStop(fragment: Int, count: Int) {
        if (fragment == Constant.TAB.NEW) {
            newCount = count;
            if (count == 0 && tabNew != null) {
                tabNew.clearAnimation()
                binding.contentOrders.orderTabLayout.getTabAt(fragment)!!.text = "NEW";
                tabNew.background = ContextCompat.getDrawable(this, R.drawable.tab_selector)
                binding.contentOrders.orderTabLayout.setTabTextColors(
                    Color.parseColor("#108AE5"),
                    Color.parseColor("#FFFFFF")
                );
            }
        } else if (fragment == Constant.TAB.ACTIVE) {
            if (count == 0) {
                binding.contentOrders.orderTabLayout.getTabAt(fragment)!!.text = "In House";
            }
        }
        val totalCount = newCount//+scheduleCount
        if (totalCount == 0)

            Handler(Looper.getMainLooper()).postDelayed({
                //Do something after X*1000 seconds
                stopMedia()
            }, 5 * 1000)

    }

    override fun onBackPressed() {
        stopMedia()
        super.onBackPressed()
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
    }

    override fun onStartStop(count: Int) {
        if (count > 0) {
            binding.contentOrders.reservationButton.setBackgroundColor(Color.RED)
            val anim = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 300 //You can manage the blinking time with this parameter
            anim.startOffset = 20
            anim.repeatMode = Animation.REVERSE
            anim.repeatCount = Animation.INFINITE
            binding.contentOrders.reservationButton.startAnimation(anim)
        } else {
            binding.contentOrders.reservationButton.clearAnimation()
            binding.contentOrders.reservationButton.background = getDrawable(R.drawable.gray_bg_one)
        }
    }

    var pushBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //showToast("broadcast>>>>>")
            val pushMessage =
                intent?.getParcelableExtra<PushMessage>(MyFirebaseMessagingService.PUSH_KEY)
            println("datapush>>>>> ${pushMessage?.let { Util.getStringFromBean(it) }}")
            pushMessage?.let { handlePushMessage(it) }

        }

    }

    private fun handlePushMessage(pushMessage: PushMessage) {
        try {
            pushMessage.let {
                if (it.type.equals(
                        TYPE.OrderNew.toString(),
                        true
                    ) || it.type.equals(TYPE.OrderHold.toString(), true)
                ) {
                    //  var fragment:NewFragment = samplePagerAdapter.getItem(0) as NewFragment
                    ///fragment.callService(this@OrdersActivity,rsLoginResponse?.data?.restaurantId!!)
                    val fragment: Fragment =
                        orderViewPager.adapter?.instantiateItem(
                            orderViewPager,
                            orderViewPager.currentItem
                        ) as Fragment

                    if (fragment is CompletedFragment) {
                        //println("complete>>>>>>>>")
                        callNewOrderService()
                    } else {
                        //println("other>>>>>>>>")
                        callService(true)
                    }
                    //fragment.callService()
                }
        //                 if(it.type.equals(TYPE.OrderHold.toString(),true)){
        //                     ActiveFragment.getInstance()?.callService()
        //                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"")
        }
    }

    private fun createViewModel(): OrderViewModel =
        ViewModelProviders.of(this).get(OrderViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) binding.progressBar?.visibility = View.VISIBLE else binding.progressBar?.visibility = View.GONE
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            it?.let { this.showSnackBar(binding.progressBar, it) }
        })
        viewModel.orderResponse.observe(this, Observer<OrderResponse> {
            it?.let {
                newOrderList = it.data?: emptyList()
                if (newOrderList.isNotEmpty()) {
                    onFragmentInteraction(0, newOrderList.size)
                }else{
                    stopMedia()
                    onStop(Constant.TAB.NEW, 0)
                }

                //println(("item count>>>>>> " + it.reservation) ?: 0)
                val reservationCount = it.reservation ?: 0
                onStartStop(reservationCount)
                NewFragment.getInstance()?.updateNewAdapter(newOrderList)
            }
        })

        viewModel.printLastOrder.observe(this, Observer<Boolean> {
            onStop(Constant.TAB.NEW, newOrderList.size)
            try {
                it?.let {
                    if (it) {
//                        confirmData = viewModel.getOrderAt(0)
//                        val  printStatus =  Intent("com.gb.restaurant.utils.returnPrintStatus");
//                        printStatus.putExtra("PRINT_STATUS", 1);
//                        this.sendBroadcast(printStatus);
//                        return@Observer
                        //LocalBroadcastManager.getInstance(context).sendBroadcast(printStatus);
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ), 1
                            )
                        } else {
                            if (Utils.isLocationEnabled(this) ==true) {
                                if (sessionManager!!.getPrinterAddress().isNotEmpty()) {
                                    Handler().postDelayed({
                                        print(
                                            sessionManager!!.getPrinterType(),
                                            sessionManager!!.getPrinterAddress(),
                                            viewModel.getOrderAt(0)
                                        )
                                    }, 5*1000)
                                }
                                onStop(Constant.TAB.NEW, newOrderList.size)
                            }

                        }

                    }


                }
            }catch (e:Exception){
                e.printStackTrace()
            }

        })
        viewModel.orderStatusResponse.observe(this, Observer<OrderStatusResponse> {
            it?.let {
                if (it.status == Constant.STATUS.FAIL) {
                   showToast(it.result?:"")
                } else {
                    showToast(it.result?:"")
                    //callService(false)
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        viewModel.rmAndUpdateOrderList(it.data?.id?:"",true)
                    }

                    //(activity as OrdersActivity?)!!.refreshActiveFragment()

                }
            }
        })


    }

    fun callService(isPrintLastOrder: Boolean) {
        try {
            if (Validation.isOnline(this)) {
                // fragmentBaseActivity.showToast("broadcast new")
                var orderRequest = OrderRequest()
                orderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId?:""
                orderRequest.service_type =
                    Constant.SERVICE_TYPE.GET_NEW_ORDER//Constant.SERVICE_TYPE.GET_NEW_ORDER
                orderRequest.deviceversion = Util.getVersionName(this)

                viewModel.getOrderResponse(orderRequest, isPrintLastOrder)
            } else {
                showSnackBar(
                    binding.progressBar,
                    getString(R.string.internet_connected)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(BaseFragment.TAG, e.message?:"")
        }
    }

    fun print(printerType: Int, printerId: String, data: Data?) {
        data?.let {
            val printSize = sessionManager?.getPrintPageSize()?:1
            mCanvas = Canvas(canvasBitmap)
            mBitmap = Utils.createOrderReceipt(MyApp.instance, mCanvas, 576, it)
            if (mBitmap != null) {
                confirmData = it
                //Print Munbyn
                Utils.munbynPrinting(
                    MyApp.instance,
                    mBitmap,
                    printerType,
                    printerId,
                    printSize
                )


            }
        }


    }
    private val printStatusBroadcast:BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1?.let {
                //println("printStatusBroadcast>>>>")
                val status = it.getIntExtra("PRINT_STATUS",0)
                //println("printStatusBroadcast>>>> $status")
                if(status==1){
                    confirmNewOrder("" + confirmData?.orderid, "" + confirmData?.type?:"")
                }
            }

        }
    }

    private fun confirmNewOrder(orderId: String, orderType: String) {
        val orderStatusRequest = OrderStatusRequest(deviceversion=Util.getVersionName(MyApp.instance),
            status=Constant.ORDER_STATUS.CONFIRMED ,
            order_id=orderId)
        if (orderType.equals("Delivery", true)) {
            orderStatusRequest.readytime = "${rsLoginResponse?.data?.deliverytime?.get(0)} minutes"
        } else {
            orderStatusRequest.readytime = "${rsLoginResponse?.data?.pickuptime?.get(0)} minutes"
        }

        try {
            if (Validation.isOnline(this)) {
                orderStatusRequest.restaurant_id = rsLoginResponse?.data?.restaurantId?:""
               // println("request>>>>> ${Util.getStringFromBean(orderStatusRequest)}")
                viewModel.orderStatus(orderStatusRequest)
            } else {
                showToast(getString(R.string.internet_connected))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(BaseFragment.TAG, e.message?:"")
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RESERVATION) {
                callNewOrderService()
            }
        }
    }

}
