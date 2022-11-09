package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.viewmodel.AddTipsViewModel

class GbSummAdapter(val mContext:Context, var viewModel: AddTipsViewModel) : RecyclerView.Adapter<GbSummAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.daily_order_summary, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.getGBDailySummSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        viewModel.getGBDailySummAt(position)?.let {
            holder.apply {
                keyText.text = "${it.key}"
                priceText.text="${it.value}"
            }
        }


    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val keyText by lazy { itemView.findViewById(R.id.key_text) as TextView }
        val priceText by lazy { itemView.findViewById(R.id.price_text) as TextView }


        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
           // supportClickListener.onItemClick(adapterPosition, v)
        }
    }

}