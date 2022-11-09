package com.gb.restaurant.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.viewmodel.AddTipsViewModel
import com.gb.restaurant.model.addtips.Data

class AddingTipsAdapter(val mContext:Context, var viewModel: AddTipsViewModel) : RecyclerView.Adapter<AddingTipsAdapter.OrderViewHolder>() {

    lateinit var addingTipsListener: AddingTipsListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.add_tips_item, parent, false))
    }

    override fun getItemCount(): Int {
       return viewModel.getActiveOrderSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        viewModel.getActiveOrderAt(position)?.let {
             holder.apply {
                 var date2 = it.date2
                 var dateArray = date2?.split("\\s".toRegex())
                 var month = dateArray?.get(0) ?:""
                 var day = dateArray?.get(1) ?:""
                 monthText.text = month
                 dateText.text =day
                 nameText.text ="${it.name}"
                 desText.text="${it.delivery}"
                // desText.text ="${it.de}"
                 idText.text ="${it.id}"
                 var statusType = "${it.type?:""}"
                 statusType = if(it.payment.equals("Pending",true)){
                     "$statusType Not Paid"
                 }else{
                     "$statusType Paid"
                 }
                 statusText.text = "$statusType"
                 if(it?.tips2.isNullOrEmpty() ) {
                     addTipsText.text = "Add Tips"
                     addTipsText.alpha=1.0f
                 }else{
                     addTipsText.text = "Added"
                     addTipsText.textSize = 10.0F
                     addTipsText.alpha=0.5f
                 }

                 if(!it.status.equals("Confirmed",true)){
                     addTipsText.alpha=0.5f
                 }else{
                     addTipsText.alpha=1.0f
                 }
                }

        }
        holder.addTipsText.setOnClickListener {
            if(viewModel.getActiveOrderAt(position)?.status.equals("Confirmed",true)) {
                if (viewModel.getActiveOrderAt(position)?.tips2.isNullOrEmpty()) {
                    addingTipsListener.onItemClick(
                        viewModel.getActiveOrderAt(position),
                        holder.addTipsText
                    )
                } else {
                    Toast.makeText(mContext, "Tips already added", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    fun setOnItemClickListener(addingTipsListener: AddingTipsListener) {
        this.addingTipsListener = addingTipsListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val monthText by lazy { itemView.findViewById(R.id.mothText) as TextView }
        val dateText by lazy { itemView.findViewById(R.id.dateText) as TextView }
        val nameText by lazy { itemView.findViewById(R.id.nameText) as TextView }
        val desText by lazy { itemView.findViewById(R.id.desText) as TextView }
        val idText by lazy { itemView.findViewById(R.id.idText) as TextView }
        val statusText by lazy { itemView.findViewById(R.id.statusText) as TextView }
        val addTipsText by lazy { itemView.findViewById(R.id.add_tips_text) as TextView }

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
          //  addingTipsListener.onItemClick(viewModel.getActiveOrderAt(adapterPosition), v)
        }
    }

    interface AddingTipsListener {
        fun onItemClick(data:Data?, v: View)
    }
}