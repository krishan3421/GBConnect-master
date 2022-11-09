package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.model.discount.getdiscount.Discount
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.viewmodel.CouponViewModel
import com.gb.restaurant.viewmodel.OrderViewModel
import com.gb.restaurant.viewmodel.SupportViewModel

class MarketingAdapter(val mContext:Context, var viewModel: CouponViewModel) : RecyclerView.Adapter<MarketingAdapter.OrderViewHolder>() {

    lateinit var marketClickListener: MarketClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.marketing_item, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.getOfferSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        viewModel.getOfferAt(position)?.let {
             holder.apply {
                 offerNumText.text="Offer ${position+1}"
                 statusText.text="${it.status}"
                 spendText.text="Spend Minimum $"
                 minimumText.text="${it.minorder}"
                 getText.text="Get ${it.types} off"
                 offerText.text="${it.details}"
            }
        }

        holder.editButton.setOnClickListener {
            marketClickListener.onEditItemClick( viewModel.getOfferAt(position)!!,position, it)
        }
        holder.resetButton.setOnClickListener {
            marketClickListener.onResetItemClick( viewModel.getOfferAt(position)!!,position, it)
        }
    }

    fun setOnItemClickListener(marketClickListener: MarketClickListener) {
        this.marketClickListener = marketClickListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val offerNumText by lazy { itemView.findViewById(R.id.offer_num_text) as TextView }
        val statusText by lazy { itemView.findViewById(R.id.status) as TextView }
        val spendText by lazy { itemView.findViewById(R.id.spend_text) as TextView }
        val minimumText by lazy { itemView.findViewById(R.id.min_text) as TextView }
        val editButton by lazy { itemView.findViewById(R.id.edit_button) as Button }
        val getText by lazy { itemView.findViewById(R.id.get_text) as TextView }
        val offerText by lazy { itemView.findViewById(R.id.off_text) as TextView }
        val resetButton by lazy { itemView.findViewById(R.id.reset_button) as TextView }


        init {
            //updateButton.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            when(v){
                /*updateButton->{
                    newOrClickListener.onItemClick( viewModel.getOrderAt(adapterPosition)!!, v)
                }*/
            }

        }
    }

    interface MarketClickListener {
        fun onEditItemClick(discount: Discount, position:Int, v: View)
        fun onResetItemClick(discount: Discount, position:Int, v: View)
    }

}