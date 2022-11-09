package com.gb.restaurant.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.model.order.Item


class ViewDialogAdapter(val mContext:Context,var data: Data, var list: MutableList<Item>) : RecyclerView.Adapter<ViewDialogAdapter.OrderViewHolder>() {

    lateinit var reportClickListener: ReportClickListener
    companion object{
        private val TYPE_FOOTER = 1
        private val TYPE_ITEM = 2
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {

        if(viewType == TYPE_ITEM) {
            return OrderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.view_dialog_item,
                    parent,
                    false
                )
            )
        }else{
            return OrderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.view_footer,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
       return list.size+1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == list.size) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        if(position <= list.size-1) {
            var item = list[position]
            holder.apply {
                if (item.isItem) {
                    headerLayout.visibility = View.VISIBLE
                    headerText.text = item.heading
                    priceText.text = "$${String.format("%.02f", item.price)}"
                    priceText.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                    if(!item.qty.isNullOrEmpty()) {
                        quantityText.visibility =View.VISIBLE
                        quantityText.text = "${item.qty}"
                        quantityText.typeface = Typeface.DEFAULT
                    }else{
                        quantityText.visibility =View.INVISIBLE
                    }

                    if(!item.extra.isNullOrEmpty()) {
                        extraText.text = "${item.extra}"
                        extraText.visibility = View.VISIBLE
                    }else{
                        extraText.visibility = View.GONE
                    }
                    if(!item.instruction.isNullOrEmpty()) {
                        instructionText.text = "${item.extra}"
                        instructionText.visibility = View.VISIBLE
                    }else{
                        instructionText.visibility = View.GONE
                    }
                } else {
                    headerLayout.visibility = View.INVISIBLE
                    quantityText.text = "${item.heading}"
                    if(item.isOffer){
                        priceText.text = "${item.heading}"
                        priceText.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                        quantityText.visibility = View.INVISIBLE
                    }else{
                        priceText.text = "$${String.format("%.02f", item.price)}"
                        priceText.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                        quantityText.visibility = View.VISIBLE
                    }
                }

                if(item.isBold){
                    quantityText.typeface = Typeface.DEFAULT_BOLD
                    priceText.typeface = Typeface.DEFAULT_BOLD
                }else{
                    quantityText.typeface = Typeface.DEFAULT
                    priceText.typeface = Typeface.DEFAULT
                }

                if(item.isDividerLine){
                    dividerLine.visibility = View.VISIBLE
                }else{
                    dividerLine.visibility = View.GONE
                }
            }
        }else{
            if(!data.name.isNullOrEmpty())
            holder.nameText.text = "${data.name}"
            if(!data.mobile.isNullOrEmpty())
            holder.phoneText.text = "${data.mobile}"
            if(!data.delivery.isNullOrEmpty()) {
                holder.addressText.visibility = View.VISIBLE
                holder.addressText.text = "${data.delivery}"
            }else{
                holder.addressText.visibility = View.GONE
            }
        }
    }

    fun setOnItemClickListener(reportClickListener: ReportClickListener) {
        this.reportClickListener = reportClickListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerText by lazy { itemView.findViewById(R.id.heading) as TextView }
        val quantityText by lazy { itemView.findViewById(R.id.quantity_text) as TextView }
        val priceText by lazy { itemView.findViewById(R.id.price_text) as TextView }
        val nameText by lazy { itemView.findViewById(R.id.name_text) as TextView }
        val phoneText by lazy { itemView.findViewById(R.id.phone_text) as TextView }
        val dividerLine by lazy { itemView.findViewById(R.id.divider_line) as View }
        val extraText by lazy { itemView.findViewById(R.id.extra_text) as TextView }
        val instructionText by lazy { itemView.findViewById(R.id.instruction_text) as TextView }
        val headerLayout by lazy { itemView.findViewById(R.id.header_layout) as LinearLayout }
        val addressText by lazy { itemView.findViewById(R.id.address_text) as TextView }

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            //reportClickListener.onItemClick(adapterPosition, v)
        }
    }

    fun addAll(mylist: List<Item>) {
        list.clear()
        list.addAll(mylist)
        this.notifyDataSetChanged()
    }

    interface ReportClickListener {
        fun onItemClick(position: Int, v: View)
    }
}