package com.gb.restaurant.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.gb.restaurant.R

/**
 * A simple [Fragment] subclass.
 */
class MoreSaleFragment : BaseFragment() {

    companion object{
        private val TAG = MoreSaleFragment::class.java.simpleName
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more_sale, container, false)
    }

}
