package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.gb.restaurant.databinding.ActivityViewInvoiceBinding


class ViewInvoiceActivity : BaseActivity() {


    companion object{
        private val TAG = ViewInvoiceActivity::class.java.simpleName
        const val INVOICE:String = "INVOICE"
    }
    private lateinit var binding: ActivityViewInvoiceBinding
    private var url :String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()
       // setContentView(R.layout.activity_view_invoice)
        binding = ActivityViewInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }

    private fun statusBarTransparent(){
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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

    private fun initData(){
        try{
           // println("date>>>>>> ${Util.getMM_day_yyyy(Date())}")
            intent?.let {
                url = it.getStringExtra(INVOICE)!!
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

    private fun initView(){
        try{
            binding.customAppbar.backLayout.setOnClickListener {
                onBackPressed()
            }
            //web_invoice.clearCache(false);
          //  web_invoice.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
            println("url>>>> $url")
            binding.contentViewSummary.apply {
                webInvoice.settings.javaScriptEnabled = true;
                webInvoice.settings.loadWithOverviewMode = true;
                webInvoice.settings.useWideViewPort = true;
                binding.progressBar.visibility=View.VISIBLE
                webInvoice.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        binding.progressBar.visibility=View.VISIBLE
                        view!!.loadUrl(request?.url.toString())
                        return true
                    }
                    override fun onPageFinished(view: WebView, url: String) {
                        binding.progressBar.visibility=View.GONE
                    }

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        binding.progressBar.visibility=View.GONE
                       // showToast(error.description.)
                    }

                }
                webInvoice.loadUrl(url);
            }

        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
            binding.progressBar.visibility=View.GONE
        }
    }
    fun backClick(v: View){
        onBackPressed()
    }

}
