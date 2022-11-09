package com.gb.restaurant.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.gb.restaurant.R
import com.gb.restaurant.model.PrinterModel

internal class PrinterListAdapter(private var printerList: List<PrinterModel>) :

    RecyclerView.Adapter<PrinterListAdapter.PrinterHolder>() {


    var onItemClick: ((PrinterModel) -> Unit)? = null

    internal inner class PrinterHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtName: TextView = view.findViewById(R.id.txtName)
        var txtAddress: TextView = view.findViewById(R.id.txtAddress)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(printerList[adapterPosition])
            }
        }
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrinterHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_printer, parent, false)
        return PrinterHolder(itemView)
    }

    override fun onBindViewHolder(holder: PrinterHolder, position: Int) {
        val printerModel = printerList[position]
        holder.txtName.text = printerModel.printer_name
        holder.txtAddress.text = printerModel.printer_id
    }

    override fun getItemCount(): Int {
        return printerList.size
    }
}