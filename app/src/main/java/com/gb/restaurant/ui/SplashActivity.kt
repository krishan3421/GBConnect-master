package com.gb.restaurant.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import com.gb.restaurant.R
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : BaseActivity() {

    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 3000 //2 seconds
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setSupportActionBar(toolbar)
        toolbar.visibility = View.GONE

        //Initializing the Handler
        mDelayHandler = Handler()

        //Navigate with delay
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        when (metrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> {
                println("density>>>>>>>>> DENSITY_LOW")
            }

            DisplayMetrics.DENSITY_MEDIUM -> {
                println("density>>>>>>>>> DENSITY_MEDIUM")
            }

            DisplayMetrics.DENSITY_HIGH -> {
                println("density>>>>>>>>> DENSITY_HIGH")
            }
            DisplayMetrics.DENSITY_XHIGH -> {
                println("density>>>>>>>>> DENSITY_XHIGH")
            }
            DisplayMetrics.DENSITY_XXHIGH -> {
                println("density>>>>>>>>> DENSITY_XXHIGH")
            }
            DisplayMetrics.DENSITY_XXXHIGH -> {
                println("density>>>>>>>>> DENSITY_XXXHIGH")
            }
        }
        println("density>>>>>>>>> ${metrics.densityDpi}")
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private val mRunnable: Runnable = Runnable {

        var  intent = Intent(applicationContext, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
