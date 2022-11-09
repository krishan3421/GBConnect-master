package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R

class NewReportAdapter(val mContext:Context, var list:MutableList<String>) : RecyclerView.Adapter<NewReportAdapter.OrderViewHolder>() {

    lateinit var reportClickListener: ReportClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.new_report_item, parent, false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.apply {
            reportText.text="${list[position]}"
        }

    }

    fun setOnItemClickListener(reportClickListener: ReportClickListener) {
        this.reportClickListener = reportClickListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val reportText by lazy { itemView.findViewById(R.id.report_text) as TextView }
        val reportImage by lazy { itemView.findViewById(R.id.report_image) as ImageView }


        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            reportClickListener.onItemClick(adapterPosition, v)
        }
    }

    interface ReportClickListener {
        fun onItemClick(position: Int, v: View)
    }
}