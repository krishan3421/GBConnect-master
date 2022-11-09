package com.gb.restaurant.ui.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
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

class OrdersWeeklyAdapter(val mContext:Context, var viewModel: ReportViewModel) : RecyclerView.Adapter<OrdersWeeklyAdapter.OrderViewHolder>() {

    companion object{
        val TAG = OrdersWeeklyAdapter::class.java.simpleName
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.orders_weekly_item, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.getReportSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        viewModel.getReportAt(position)?.let {
            holder.apply {
                dateText.text ="${it.date}"
                if(it.invoice =="View Invoice"){
                    statusText.text ="Generated"
                    statusText.setTextColor(ContextCompat.getColor(mContext,R.color.green))
                    nextIcon.visibility=View.VISIBLE
                }else{
                    statusText.text ="Awaiting"
                    statusText.setTextColor(ContextCompat.getColor(mContext,R.color.dark_gray))
                    nextIcon.visibility=View.GONE
                }
            }
        }
        holder.statusText.setOnClickListener {
            try{
                if(viewModel.getReportAt(position)?.invoice =="View Invoice") {
                    var intent = Intent(mContext, ViewInvoiceActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra(ViewInvoiceActivity.INVOICE, "${viewModel.getReportAt(position)?.orderurl}${MyApp.instance.deviceToken}")
                    mContext.startActivity(intent)
                }
            }catch (e:Exception){
                e.printStackTrace()
                Log.e(TAG,e.message!!)
            }
        }

        holder.nextIcon.setOnClickListener {
            try{
                if(viewModel.getReportAt(position)?.invoice =="View Invoice") {
                    var intent = Intent(mContext, ViewInvoiceActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra(ViewInvoiceActivity.INVOICE, "${viewModel.getReportAt(position)?.orderurl}${MyApp.instance.deviceToken}")
                    mContext.startActivity(intent)
                }
            }catch (e:Exception){
                e.printStackTrace()
                Log.e(TAG,e.message!!)
            }
        }

    }


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val dateText by lazy { itemView.findViewById(R.id.date_text) as TextView }
        val statusText by lazy { itemView.findViewById(R.id.status_text) as TextView }
        val nextIcon by lazy { itemView.findViewById(R.id.next_icon) as ImageView }


        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            if(viewModel.getReportAt(adapterPosition)?.invoice =="View Invoice") {
                var intent = Intent(mContext, ViewInvoiceActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(ViewInvoiceActivity.INVOICE, "${viewModel.getReportAt(adapterPosition)?.orderurl}${MyApp.instance.deviceToken}")
                mContext.startActivity(intent)
            }
            //reportClickListener.onItemClick(adapterPosition, v)
        }
    }

}