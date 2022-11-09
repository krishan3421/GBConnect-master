package com.gb.restaurant.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.ui.ConfirmTimeDialogActivity
import com.gb.restaurant.ui.ViewDialogActivity
import com.gb.restaurant.viewmodel.OrderViewModel

class ScheduleAdapter(val mContext:Context, var viewModel: OrderViewModel) : RecyclerView.Adapter<ScheduleAdapter.OrderViewHolder>() {

    lateinit var newOrClickListener: NewOrClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.getOrderSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        viewModel.getOrderAt(position)?.let {
            holder.apply {
                restaurantId.text = "${it.id}"
                if(!it.name.isNullOrEmpty())
                nameText.text ="${it.name}"

                if(!it.mobile.isNullOrEmpty())
                mobileText.text = "${it.mobile}"
                totalPrice.text = "$${it.total}"
                dateText.text = "FUTURE ORDER FOR ${it.holddate2}"

                if(it.payment!!.contains("Pending",true)){
                    typeText.text = "${it.type?.toUpperCase()} CASH"
                    typeText.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                }else{
                    typeText.text = "${it.type?.toUpperCase()} PAID"
                    typeText.setTextColor(ContextCompat.getColor(mContext, R.color.green))
                }
            }
        }

        holder.confirmButton.setOnClickListener {
            var intent = Intent(mContext,ConfirmTimeDialogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(ConfirmTimeDialogActivity.ORDER_ID, viewModel.getOrderAt(position)!!.id)
            intent.putExtra(ConfirmTimeDialogActivity.HOLD, viewModel.getOrderAt(position)!!.hold)
            mContext.startActivity(intent)
        }

        holder.viewLayout.setOnClickListener {
            MyApp.instance.data = viewModel.getOrderAt(position)
            var  intent = Intent(mContext, ViewDialogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(ViewDialogActivity.FROMPAGE,0)
            mContext.startActivity(intent)
        }
    }

    fun setOnItemClickListener(newOrClickListener: NewOrClickListener) {
        this.newOrClickListener = newOrClickListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val restaurantId by lazy { itemView.findViewById(R.id.restaurant_id) as TextView }
        val nameText by lazy { itemView.findViewById(R.id.name_text) as TextView }
        val mobileText by lazy { itemView.findViewById(R.id.mobile_text) as TextView }
        val totalPrice by lazy { itemView.findViewById(R.id.total_text) as TextView }
        val dateText by lazy { itemView.findViewById(R.id.date_text) as TextView }
      val confirmButton by lazy { itemView.findViewById(R.id.confirm_button) as Button }
        val viewLayout by lazy { itemView.findViewById(R.id.view_layout) as LinearLayout }
        val typeText by lazy { itemView.findViewById(R.id.type_text) as TextView }

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            newOrClickListener.onItemClick(adapterPosition, v)
        }
    }

    interface NewOrClickListener {
        fun onItemClick(position: Int, v: View)
    }
}