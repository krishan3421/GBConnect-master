package com.gb.restaurant.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.gb.restaurant.ui.BaseActivity

abstract class BaseFragment : Fragment() {


    lateinit var fragmentBaseActivity: BaseActivity
    companion object{
        val TAG = BaseFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentBaseActivity = this.activity as BaseActivity
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        try {
            super.onActivityCreated(savedInstanceState)
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.getWindowToken(), 0)
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            e.printStackTrace()
        }

    }

    /*
       * This method is used for Hide the keyboard.
       *
        */
    fun hideKeyBoard() {
        try {
            if (fragmentBaseActivity != null) {
                val imm = fragmentBaseActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm.isAcceptingText && view != null && view?.getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(view?.getWindowToken(), 0)
                }
            }


        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }
}

