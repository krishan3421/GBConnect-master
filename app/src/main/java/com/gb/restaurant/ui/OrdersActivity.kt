package com.gb.restaurant.ui

import android.app.Activity
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.MediaPlayer
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.gb.restaurant.Constant
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.Validation
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.PrinterModel
import com.gb.restaurant.model.order.OrderRequest
import com.gb.restaurant.model.order.OrderResponse
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.push.MyFirebaseMessagingService
import com.gb.restaurant.push.PushMessage
import com.gb.restaurant.push.TYPE
import com.gb.restaurant.ui.fragments.*
import com.gb.restaurant.utils.BluetoothDiscovery
import com.gb.restaurant.utils.IpScanner
import com.gb.restaurant.utils.Util
import com.gb.restaurant.utils.Utils
import com.gb.restaurant.viewmodel.OrderViewModel
import com.grabull.session.SessionManager
import kotlinx.android.synthetic.main.activity_orders.progress_bar
import kotlinx.android.synthetic.main.content_orders.*
import kotlinx.android.synthetic.main.custom_appbar.*
import kotlinx.android.synthetic.main.fragment_new.*
import java.util.ArrayList

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
    companion object {
        private val TAG: String = OrdersActivity::class.java.simpleName
        var isPageVisible: Boolean = false
        private val RESERVATION: Int = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        setContentView(R.layout.activity_orders)
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
            back_layout.setOnClickListener {
                onBackPressed()
            }

            back_layout.visibility = View.INVISIBLE
            samplePagerAdapter = SamplePagerAdapter(supportFragmentManager)
            order_view_pager.adapter = samplePagerAdapter
            order_tab_layout.setupWithViewPager(order_view_pager)
            order_view_pager.addOnPageChangeListener(this)
            orderViewPager = order_view_pager
            tabNew = (order_tab_layout.getChildAt(0) as ViewGroup).getChildAt(Constant.TAB.NEW)
//             tabNewText =
//                order_tab_layout.getChildAt(0).findViewById(id.title) as TextView
//             tabNewText.setTextColor(ContextCompat.getColor(this,R.color.colorAccent))
            //tabSchedule = (order_tab_layout.getChildAt(0) as ViewGroup).getChildAt(Constant.TAB.SCHEDULE)

            for (i in 0 until order_tab_layout.tabCount) {
                val tab = (order_tab_layout.getChildAt(0) as ViewGroup).getChildAt(i)
                val p = tab.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(0, 0, 10, 0)
                tab.requestLayout()
            }
            mediaPlayer = MediaPlayer.create(this, R.raw.sound);
            mediaPlayer.isLooping = true
            //blinkTab(1)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    fun callNewOrderService() {
        try {
            if (Validation.isOnline(this)) {
                // fragmentBaseActivity.showToast("broadcast new")
                var orderRequest = OrderRequest()
                orderRequest.restaurant_id = rsLoginResponse?.data?.restaurantId!!
                orderRequest.service_type =
                    Constant.SERVICE_TYPE.GET_NEW_ORDER//Constant.SERVICE_TYPE.GET_NEW_ORDER
                orderRequest.deviceversion = Util.getVersionName(this)
                // println("new request>>>>> ${Util.getStringFromBean(orderRequest)}")
                viewModel.getOrderResponse(orderRequest, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(BaseFragment.TAG, e.message!!)
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
            Log.e(TAG, e.message!!)
        }
    }

    private fun statusBarTransparent() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
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

    override fun onResume() {
        super.onResume()
        isPageVisible = true
        LocalBroadcastManager.getInstance(this).registerReceiver(
            pushBroadcastReceiver,
            IntentFilter(MyFirebaseMessagingService.PUSHBROADCAST)
        )
       // mainHandler.post(updateTextTask)

    }
    override fun onPause() {
        super.onPause()
        isPageVisible = false
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
                    order_tab_layout.getTabAt(position)!!.text = "NEW($count)";
                    blinkTab(tabNew, Constant.TAB.NEW)
                }
                Constant.TAB.ACTIVE -> {
                    order_tab_layout.getTabAt(position)!!.text = "In House($count)";
                    //blinkTab(tabSchedule,Constant.TAB.SCHEDULE)
                }
                else -> {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
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
            Log.e(TAG, e.message!!)
        }
    }

    fun refreshPage(v: View) {
        try {
            val aniRotate: Animation =
                AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anti_clockwise)
            refresh_image.startAnimation(aniRotate)
            order_view_pager.adapter?.notifyDataSetChanged()
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
                order_view_pager.adapter?.notifyDataSetChanged()
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
            Log.e(TAG, e.message!!)
        }
    }

    fun searchPage() {
        try {
            var intent = Intent(applicationContext, SearchActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun eventEnquieryPage(view: View) {
        try {
            var intent = Intent(applicationContext, EventEnquiryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    fun startMedia() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying && MyApp.instance.isAlarm) {
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
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
            Log.e(TAG, e.message!!)
        }
    }

    override fun onStop(fragment: Int, count: Int) {
        if (fragment == Constant.TAB.NEW) {
            newCount = count;
            if (count == 0 && tabNew != null) {
                tabNew.clearAnimation()
                order_tab_layout.getTabAt(fragment)!!.text = "NEW";
                tabNew.background = ContextCompat.getDrawable(this, R.drawable.tab_selector)
                order_tab_layout.setTabTextColors(
                    Color.parseColor("#108AE5"),
                    Color.parseColor("#FFFFFF")
                );
            }
        } else if (fragment == Constant.TAB.ACTIVE) {
            if (count == 0) {
                order_tab_layout.getTabAt(fragment)!!.text = "In House";
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
            reservation_button.setBackgroundColor(Color.RED)
            val anim = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 300 //You can manage the blinking time with this parameter
            anim.startOffset = 20
            anim.repeatMode = Animation.REVERSE
            anim.repeatCount = Animation.INFINITE
            reservation_button.startAnimation(anim)
        } else {
            reservation_button.clearAnimation()
            reservation_button.background = getDrawable(R.drawable.gray_bg_one)
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
                        NewFragment.getInstance()?.callService(true)
                    }
                    //fragment.callService()
                }
        //                 if(it.type.equals(TYPE.OrderHold.toString(),true)){
        //                     ActiveFragment.getInstance()?.callService()
        //                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
    }

    private fun createViewModel(): OrderViewModel =
        ViewModelProviders.of(this).get(OrderViewModel::class.java).also {
            ComponentInjector.component.inject(it)
        }

    private fun showLoadingDialog(show: Boolean) {
        if (show) progress_bar?.visibility = View.VISIBLE else progress_bar?.visibility = View.GONE
    }

    private fun attachObserver() {
        viewModel.isLoading.observe(this, Observer<Boolean> {
            it?.let { showLoadingDialog(it) }
        })
        viewModel.apiError.observe(this, Observer<String> {
            if (new_swipe_refresh != null)
                new_swipe_refresh.isRefreshing = false
            it?.let { this.showSnackBar(progress_bar, it) }
        })
        viewModel.orderResponse.observe(this, Observer<OrderResponse> {
            it?.let {
                var list = it.data
                if (list?.size!! > 0)
                    onFragmentInteraction(0, list?.size!!)

                println("item count>>>>>> " + it.reservation ?: 0)
                var reservationCount = it.reservation ?: 0
                onStartStop(reservationCount)
            }
        })


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
