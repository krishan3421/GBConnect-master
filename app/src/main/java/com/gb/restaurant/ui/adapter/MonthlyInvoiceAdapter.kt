package com.gb.restaurant.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.ui.ViewInvoiceActivity
import com.gb.restaurant.viewmodel.ReportViewModel

class MonthlyInvoiceAdapter(val mContext:Context, var viewModel: ReportViewModel) : RecyclerView.Adapter<MonthlyInvoiceAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.monthly_invoice_item, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.getReportSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        viewModel.getReportAt(position)?.let {
            holder.apply {
                dateText.text ="${it.date}"
                statusText.text ="${it.payment}"
                if(it.payment.equals("Pending",true)){
                    statusText.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark_one))
                    viewText.text = "View"
                    statusText.visibility=View.VISIBLE
                    viewText.setTextColor(ContextCompat.getColor(mContext,R.color.blue_color))
                    nextIcon.visibility = View.VISIBLE
                }else if(it.payment.equals("Paid",true)){
                    statusText.visibility=View.VISIBLE
                    viewText.text = "View"
                    statusText.setTextColor(ContextCompat.getColor(mContext,R.color.green))
                    viewText.setTextColor(ContextCompat.getColor(mContext,R.color.blue_color))
                    nextIcon.visibility = View.VISIBLE
                }else{
                    statusText.visibility=View.INVISIBLE
                    viewText.text = "Report Awaiting"
                    viewText.setTextColor(ContextCompat.getColor(mContext,R.color.dark_gray))
                    nextIcon.visibility = View.GONE
                }

            }
        }
        holder.viewText.setOnClickListener {
           if(holder.statusText.text.toString().equals("Pending",true) || holder.statusText.text.toString().equals("Paid",true)){
               var intent = Intent(mContext, ViewInvoiceActivity::class.java)
               intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
               intent.putExtra(ViewInvoiceActivity.INVOICE, "${viewModel.getReportAt(position)?.url}${MyApp.instance.deviceToken}")
               mContext.startActivity(intent)
           }
        }
        holder.nextIcon.setOnClickListener {
            if(holder.statusText.text.toString().equals("Pending",true) || holder.statusText.text.toString().equals("Paid",true)){
                var intent = Intent(mContext, ViewInvoiceActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(ViewInvoiceActivity.INVOICE, "${viewModel.getReportAt(position)?.url}${MyApp.instance.deviceToken}")
                mContext.startActivity(intent)
            }
        }

    }


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val dateText by lazy { itemView.findViewById(R.id.week_text) as TextView }
        val statusText by lazy { itemView.findViewById(R.id.status_text) as TextView }
        val viewText by lazy { itemView.findViewById(R.id.view_text) as TextView }
        val nextIcon by lazy { itemView.findViewById(R.id.next_icon) as ImageView }


        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            if(statusText.text.toString().equals("Pending",true) || statusText.text.toString().equals("Paid",true)){
                var intent = Intent(mContext, ViewInvoiceActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(ViewInvoiceActivity.INVOICE, "${viewModel.getReportAt(adapterPosition)?.url}${MyApp.instance.deviceToken}")
                mContext.startActivity(intent)
            }
           // reportClickListener.onItemClick(adapterPosition, v)
        }
    }

}