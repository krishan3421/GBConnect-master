package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.gb.restaurant.R
import com.gb.restaurant.enumm.ReportsDetail
import com.gb.restaurant.ui.fragments.MonthlyInvoiceFragment
import com.gb.restaurant.ui.fragments.MonthlyMealFragment
import com.gb.restaurant.ui.fragments.MoreSaleFragment
import com.gb.restaurant.ui.fragments.OrdersWeeklyFragment
import kotlinx.android.synthetic.main.custom_appbar.*


class ReportsDetailActivity : BaseActivity() {
    companion object{
        private val TAG = ReportsDetailActivity::class.java.simpleName
        public const val FRAGMENT_KEY="FRAGMENT_KEY"
    }

    private var type:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
        setContentView(R.layout.activity_reports_detail)
        initData()
        initView()

    }
    fun backClick(v: View){
        onBackPressed()
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


    private fun initData(){
        try{
            intent?.let {
                type = intent.getIntExtra(FRAGMENT_KEY,0)
            }
            println("type>>>> $type")
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initView(){
        var fragment:Fragment? = null
        try{
            back_layout.setOnClickListener {
                onBackPressed()
            }
          if(type == ReportsDetail.MONTHLY_MEALS.ordinal){
              fragment = MonthlyMealFragment()
          }else if(type == ReportsDetail.MONTHLY_INVOICE.ordinal){
              fragment = MonthlyInvoiceFragment()
          }else if(type == ReportsDetail.OTHERS.ordinal){
              fragment = OrdersWeeklyFragment()
          }else{
              fragment = MoreSaleFragment()
          }
            fragment?.let { replaceFragment(it) }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    fun replaceFragment(destFragment: Fragment) { // First get FragmentManager object.
        val fragmentManager: FragmentManager = this.supportFragmentManager
        // Begin Fragment transaction.
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        // Replace the layout holder with the required Fragment object.
        fragmentTransaction.replace(R.id.container, destFragment)
        // Commit the Fragment replace action.
        fragmentTransaction.commit()
    }


}
