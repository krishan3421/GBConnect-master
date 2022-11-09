package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.viewmodel.ReportViewModel

class MonthlyMealAdapter(val mContext:Context, var viewModel: ReportViewModel) : RecyclerView.Adapter<MonthlyMealAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.monthly_meal_item, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.getReportSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        viewModel.getReportAt(position)?.let {
            holder.apply {
                valueText.text ="${it.key}"
                priceTaxt.text ="${it.value}"
            }
        }

    }


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val valueText by lazy { itemView.findViewById(R.id.value_text) as TextView }
        val priceTaxt by lazy { itemView.findViewById(R.id.price_text) as TextView }


        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            //reportClickListener.onItemClick(adapterPosition, v)
        }
    }

}