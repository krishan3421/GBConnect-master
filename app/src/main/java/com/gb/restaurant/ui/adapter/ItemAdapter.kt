package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.model.Item

class ItemAdapter(val mContext:Context, var itemsList:MutableList<Item>) : RecyclerView.Adapter<ItemAdapter.OrderViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false))
    }

    override fun getItemCount(): Int {
       return itemsList.size
    }

    fun getPriceCount():Double{
        var totalPriceCount :Double = 0.00
        itemsList?.let { it.forEachIndexed { index, item ->
            if(!item.price.isNullOrEmpty()){
                totalPriceCount += item.price.toDouble()
            }
          }

        }
        return totalPriceCount
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        itemsList[position].let {
        holder.apply {
            itemText.text = "${it.items}"
            priceText.text = "$${it.price}"
        }
        }
        holder.cancelImage.setOnClickListener {
            itemsList.removeAt(position)
            notifyDataSetChanged()
        }
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
      val itemText by lazy { itemView.findViewById(R.id.item_text) as TextView }
        val priceText by lazy { itemView.findViewById(R.id.price_text) as TextView }
        val cancelImage by lazy { itemView.findViewById(R.id.cancel_image) as ImageView }

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
        }
    }

    interface NewOrClickListener {
        fun onItemClick(position: Int, v: View)
    }

}