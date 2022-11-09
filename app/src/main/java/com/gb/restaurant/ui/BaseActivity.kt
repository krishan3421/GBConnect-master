package com.gb.restaurant.ui

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.View
import android.widget.Toast
import com.gb.restaurant.R

import kotlinx.android.synthetic.main.activity_splash.*

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setSupportActionBar(toolbar)
    }

    /*
    *This method used for display the toast.
    *
     */
    fun showToast(msg: String) {
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }

    /*
    *This method used for display the alert in bottom of the screen.
    *
     */
    fun showSnackBar(view: View, msg: String) {
        try {
            runOnUiThread {
                Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.ok), null).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
