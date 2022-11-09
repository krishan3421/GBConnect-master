package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.model.Item

class ExtraItemAdapter(val mContext:Context, var itemsList:MutableList<String>) : RecyclerView.Adapter<ExtraItemAdapter.OrderViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.extra_item_layout, parent, false))
    }

    override fun getItemCount(): Int {
       return itemsList.size
    }


    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        itemsList[position].let {
        holder.apply {

        }
        }
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
      val checkBoxItem by lazy { itemView.findViewById(R.id.extra_item_checkbox) as CheckBox }
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
        }
    }

    interface ExtraClickListener {
        fun onItemClick(position: Int, v: View)
    }

}