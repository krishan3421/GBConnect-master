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
import com.gb.restaurant.model.order.Item


class NewDetailAdapter(val mContext:Context, var list: MutableList<Item>) : RecyclerView.Adapter<NewDetailAdapter.OrderViewHolder>() {

    lateinit var reportClickListener: ReportClickListener
    companion object{
        private const val TAG:String = "NewDetailAdapter"
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.detail_recycler_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
       return list.size
    }


    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
            var item = list[position]
            holder.apply {
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
                        instructionText.text = "${item.instruction}"
                        instructionText.visibility = View.VISIBLE
                    }else{
                        instructionText.visibility = View.GONE
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
        val dividerLine by lazy { itemView.findViewById(R.id.divider_line) as View }
        val extraText by lazy { itemView.findViewById(R.id.extra_text) as TextView }
        val instructionText by lazy { itemView.findViewById(R.id.instruction_text) as TextView }
        val headerLayout by lazy { itemView.findViewById(R.id.header_layout) as LinearLayout }

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