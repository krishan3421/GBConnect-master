package com.gb.restaurant.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.ui.OrdersActivity
import com.gb.restaurant.viewmodel.OrderViewModel

class NewAdapter(val mContext:OrdersActivity, var viewModel: OrderViewModel) : RecyclerView.Adapter<NewAdapter.OrderViewHolder>() {

    lateinit var newOrClickListener: NewOrClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.new_item, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.getOrderSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        viewModel.getOrderAt(position)?.let {
            holder.apply {
               // restaurantId.text = "${it.id}"
                var name=""
                if(it.type.equals("PICKUP",true)){
                    if(!it.name.isNullOrEmpty())
                        name = it.name?:""
                    if(!it.mobile.isNullOrEmpty())
                        name = "$name (${it.mobile?:""})"

                }else{
                    if(!it.name.isNullOrEmpty())
                        name = it.name?:""
                }
                nameText.text ="$name"

                if(!it.delivery.isNullOrEmpty())
                    addressText.text ="${it.delivery}"

                 if(it.hold.equals("Yes",true)){
                     futureOrderLayout.visibility=View.VISIBLE
                     futureOrderText.text="Future Delivery on : ${it.holddate2}"
                 }else{
                     futureOrderLayout.visibility=View.GONE
                 }
                totalPrice.text = "$${it.total}"
                if(it.date2.isNullOrEmpty()){
                    dateText.text = ""
                }else {
                    dateText.text = "Received: ${it.date2}"
                }
                if(it.payment!!.contains("Pending",true)){
                    typeText.text = "${it.type?.toUpperCase()} CASH"
                    typeText.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                }else{
                    typeText.text = "${it.type?.toUpperCase()} PAID"
                    typeText.setTextColor(ContextCompat.getColor(mContext, R.color.green))
                }
            }
        }

       /* holder.confirmButton.setOnClickListener {
            var intent = Intent(mContext, ConfirmTimeDialogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(ConfirmTimeDialogActivity.ORDER_ID,viewModel.getOrderAt(position)?.id)
            mContext.startActivity(intent)
        }*/

        holder.viewLayout.setOnClickListener {
            newOrClickListener.onItemClick(viewModel.getOrderAt(position)!!,position, it)
        }

    }

    fun setOnItemClickListener(newOrClickListener: NewOrClickListener) {
        this.newOrClickListener = newOrClickListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        //val restaurantId by lazy { itemView.findViewById(R.id.restaurant_id) as TextView }
        val nameText by lazy { itemView.findViewById(R.id.name_text) as TextView }
        val addressText by lazy { itemView.findViewById(R.id.address_text) as TextView }
        val totalPrice by lazy { itemView.findViewById(R.id.total_text) as TextView }
        val dateText by lazy { itemView.findViewById(R.id.date_text) as TextView }
        //val confirmButton by lazy { itemView.findViewById(R.id.confirm_button) as Button }
        val viewLayout by lazy { itemView.findViewById(R.id.view_layout) as Button }
        val typeText by lazy { itemView.findViewById(R.id.type_text) as TextView }
        val futureOrderText by lazy { itemView.findViewById(R.id.future_order_date_text) as TextView }
        val futureOrderLayout by lazy { itemView.findViewById(R.id.future_order_layout) as LinearLayout }


        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {

        }
    }

    interface NewOrClickListener {
        fun onItemClick(data: Data,position: Int, v: View)
    }
    companion object{
    }
}