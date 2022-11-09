package com.gb.restaurant.ui.adapter

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.gb.restaurant.R
import com.gb.restaurant.model.reservation.Data
import com.gb.restaurant.model.status.ReserStatusRequest
import com.gb.restaurant.model.users.User
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReservationViewModel
import com.gb.restaurant.viewmodel.RsLoginViewModel

class UsersAdapter(val mContext:Context, var viewModel: RsLoginViewModel) : RecyclerView.Adapter<UsersAdapter.OrderViewHolder>() {

    lateinit var userClickListener: UserClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false))
    }

    override fun getItemCount(): Int {
        return viewModel.getUsersSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        viewModel.getUserAt(position)?.let {
            holder.apply {
                 userIdText.text="${it.gbId}"
                 nameText.text="${it.name}"
                 statusText.text="${it.status}"
            }
        }

        holder.editButton.setOnClickListener {
            userClickListener.onEditClick(viewModel.getUserAt(position)!!,position,it)
        }
        holder.deleteButton.setOnClickListener {
            userClickListener.onRemoveClick(viewModel.getUserAt(position)!!,position,it)
        }

    }


    fun setOnItemClickListener(userClickListener:UserClickListener) {
        this.userClickListener = userClickListener
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val userIdText by lazy { itemView.findViewById(R.id.user_id_text) as TextView }
        val nameText by lazy { itemView.findViewById(R.id.name_text) as TextView }
        val statusText by lazy { itemView.findViewById(R.id.status_text) as TextView }
        val editButton by lazy { itemView.findViewById(R.id.edit_button) as Button }
        val deleteButton by lazy { itemView.findViewById(R.id.delete_button) as Button }

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            // reservClickListener.onItemClick(viewModel.getReservationAt(adapterPosition)!!,adapterPosition, v)
        }
    }

    interface UserClickListener {
        fun onEditClick(user:User, position: Int, v: View)
        fun onRemoveClick(user:User, position: Int, v: View)
    }



}