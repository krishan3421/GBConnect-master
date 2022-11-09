package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R

class ViewReportAdapter(val mContext:Context, var viewModel: MutableList<String>) : RecyclerView.Adapter<ViewReportAdapter.OrderViewHolder>() {

    lateinit var reportClickListener: ReportClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_report_item, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.size
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
//        viewModel.getReportAt(position)?.let {
//             holder.apply {
//                 dateText.text ="${it.date}"
//                 statusText.text ="${it.invoice}"
//                 if(it.invoice!!.contains("View Invoice",true)){
//                     statusText.setTextColor(ContextCompat.getColor(mContext,R.color.blue_color))
//                     pdfImage.visibility = View.VISIBLE
//                 }else{
//                     statusText.setTextColor(ContextCompat.getColor(mContext,R.color.dark_gray))
//                     pdfImage.visibility = View.INVISIBLE
//                 }
//                }
//        }

    }

    fun setOnItemClickListener(reportClickListener: ReportClickListener) {
        this.reportClickListener = reportClickListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
      //  val dateText by lazy { itemView.findViewById(R.id.date_text) as TextView }
       // val statusText by lazy { itemView.findViewById(R.id.status_text) as TextView }
        //val pdfImage by lazy { itemView.findViewById(R.id.pdf_image) as ImageView }

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