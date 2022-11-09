package com.gb.restaurant.ui

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import com.gb.restaurant.R

import kotlinx.android.synthetic.main.activity_driver_login.*

class DriverLoginActivity : BaseActivity() {

    companion object{
        private val TAG = DriverLoginActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)
        setSupportActionBar(toolbar)

        initView()
    }

    private fun initView(){
        try{
            toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            fab.setOnClickListener { view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG,e.message!!)
        }
    }

}
