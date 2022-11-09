package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.Constant
import com.gb.restaurant.R
import com.gb.restaurant.model.status.EnquiryStatusRequest
import com.gb.restaurant.viewmodel.ReservationViewModel

class EnquiryAdapter(val mContext:Context, var viewModel: ReservationViewModel) : RecyclerView.Adapter<EnquiryAdapter.OrderViewHolder>() {

    lateinit var reservClickListener: ReservationClickListener
    lateinit var statusClickListener: StatusClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.enquire_item, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.getReservationSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        viewModel.getReservationAt(position)?.let {
             holder.apply {
                 nameText.text ="${it.name}"
                 emailText.text ="${it.email}"
                 phoneText.text ="${it.phone}"
                 // typeForText.text ="${it.name}"
                 dateText.text ="${it.date}"
                 peopleText.text ="${it.peoples}"
                }
        }

        holder.confirmButton.setOnClickListener {
             var enquiryStatusRequest =EnquiryStatusRequest()
            enquiryStatusRequest.deviceversion = com.gb.restaurant.utils.Util.getVersionName(mContext)
            enquiryStatusRequest.status = Constant.ORDER_STATUS.CONFIRMED
            enquiryStatusRequest.enquiry_id = viewModel.getReservationAt(position)?.id!!
            statusClickListener.onButtonClick(enquiryStatusRequest)
        }

        holder.cancelButton.setOnClickListener {
            var enquiryStatusRequest =EnquiryStatusRequest()
            enquiryStatusRequest.deviceversion = com.gb.restaurant.utils.Util.getVersionName(mContext)
            enquiryStatusRequest.status = Constant.ORDER_STATUS.CANCEL
            enquiryStatusRequest.enquiry_id = viewModel.getReservationAt(position)?.id!!
            statusClickListener.onButtonClick(enquiryStatusRequest)
        }

    }

    fun setOnItemClickListener(reservClickListener: ReservationClickListener) {
        this.reservClickListener = reservClickListener
    }

    fun setOnButtonClickListener(statusClickListener:StatusClickListener) {
        this.statusClickListener = statusClickListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nameText by lazy { itemView.findViewById(R.id.name_text) as TextView }
        val emailText by lazy { itemView.findViewById(R.id.email_text) as TextView }
        val phoneText by lazy { itemView.findViewById(R.id.phone_text) as TextView }
     //   val typeForText by lazy { itemView.findViewById(R.id.type_for_text) as TextView }
        val dateText by lazy { itemView.findViewById(R.id.date_text) as TextView }
        val peopleText by lazy { itemView.findViewById(R.id.people_text) as TextView }
        val confirmButton by lazy { itemView.findViewById(R.id.confirm_button) as Button }
        val cancelButton by lazy { itemView.findViewById(R.id.cancel_button) as Button }

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            reservClickListener.onItemClick(adapterPosition, v)
        }
    }

    interface ReservationClickListener {
        fun onItemClick(position: Int, v: View)
    }

    interface StatusClickListener {
        fun onButtonClick(enquiryStatusRequest :EnquiryStatusRequest)
    }
}