package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.model.support.SupportItem

class SupportAdapter(val mContext:Context, var list:MutableList<SupportItem>) : RecyclerView.Adapter<SupportAdapter.OrderViewHolder>() {

    lateinit var supportClickListener: SupportClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.support_item, parent, false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.apply {
            if(position==0){
                supportImage.visibility = View.GONE
            }else{
                supportImage.visibility = View.VISIBLE
            }
            supportText.text="${list[position].name}"
        }

    }

    fun setOnItemClickListener(supportClickListener: SupportClickListener) {
        this.supportClickListener = supportClickListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val supportText by lazy { itemView.findViewById(R.id.support_text) as TextView }
        val supportImage by lazy { itemView.findViewById(R.id.support_image) as ImageView }


        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            supportClickListener.onItemClick(list[adapterPosition],adapterPosition, v)
        }
    }

    interface SupportClickListener {
        fun onItemClick(supportItem: SupportItem,position: Int, v: View)
    }
}