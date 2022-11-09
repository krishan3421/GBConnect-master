package com.gb.restaurant.ui.adapter

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.gb.restaurant.Constant
import com.gb.restaurant.R
import com.gb.restaurant.model.reservation.Data
import com.gb.restaurant.model.status.ReserStatusRequest
import com.gb.restaurant.utils.Util
import com.gb.restaurant.viewmodel.ReservationViewModel

class ReservationAdapter(val mContext:Context, var viewModel: ReservationViewModel) : RecyclerView.Adapter<ReservationAdapter.OrderViewHolder>() {

    lateinit var reservClickListener: ReservationClickListener
    lateinit var statusClickListener: StatusClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.reservation_item, parent, false))
    }

    override fun getItemCount(): Int {
        return viewModel.getReservationSize()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        viewModel.getReservationAt(position)?.let {
            holder.apply {
                nameText.text ="${it.name}"
              //  emailText.text ="${it.email}"
                phoneText.text ="${it.phone}"
                // typeForText.text ="${it.name}"
                if(!it.bookingtime.isNullOrEmpty() && it.bookingtime.contains("at")){
                  var bookingDate =   it.bookingtime.replace(" at","\nat",true)
                    dateText.text ="$bookingDate"
                }


                peopleText.text ="${it.peoples}"
               // detailsValue.text = "${it.details}"
                println("status>> ${it.status}")
                if(it.status!!.equals("Pending",true)){
                    //confirmButton.text = "Confirm"
                  //  replyLayout.visibility = View.GONE
                    statusImage.setImageResource(R.drawable.panding)
                }else if(it.status!!.equals("Confirmed",true)){
                    statusImage.setImageResource(R.drawable.tick_icon)
                }else if(it.status!!.equals(Constant.ORDER_STATUS.CLOSED,true)) {
                    statusImage.setImageResource(R.drawable.tick_icon)
                    statusImage.setColorFilter(ContextCompat.getColor(mContext, R.color.dark_gray), android.graphics.PorterDuff.Mode.SRC_IN);
                }else{
                    statusImage.setImageResource(R.drawable.cancel)
                   /* buttonLayout.visibility = View.GONE
                    replyLayout.visibility = View.VISIBLE
                    replyValue.text = "${it.reply}"*/
                }

            }
        }

       /* holder.confirmButton.setOnClickListener {
            openReplyPopUp(position,Constant.ORDER_STATUS.CONFIRMED)
        }

        holder.cancelButton.setOnClickListener {
            if(holder.cancelButton.text.contains("Cancel",true)){
                openReplyPopUp(position,Constant.ORDER_STATUS.CANCEL)
            }else{
                //openReplyPopUp(position,Constant.ORDER_STATUS.CLOSED)
                callConfirmService("",position,Constant.ORDER_STATUS.CLOSED)
            }

        }*/

    }

    private fun callConfirmService(reply:String,position: Int,orderStatus:String){
        try{
            println("reply>>>>>>>>>> $reply")
            var reserStatusRequest =ReserStatusRequest()
            reserStatusRequest.deviceversion = Util.getVersionName(mContext)
            reserStatusRequest.status = orderStatus
            reserStatusRequest.reply = reply
            reserStatusRequest.reservation_id = viewModel.getReservationAt(position)?.id!!
            statusClickListener.onButtonClick(reserStatusRequest)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun setOnItemClickListener(reservClickListener: ReservationClickListener) {
        this.reservClickListener = reservClickListener
    }

    fun setOnButtonClickListener(statusClickListener:StatusClickListener) {
        this.statusClickListener = statusClickListener
    }
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nameText by lazy { itemView.findViewById(R.id.name_text) as TextView }
     //   val emailText by lazy { itemView.findViewById(R.id.email_text) as TextView }
        val phoneText by lazy { itemView.findViewById(R.id.phone_text) as TextView }
       // val typeForText by lazy { itemView.findViewById(R.id.type_for_text) as TextView }
        val dateText by lazy { itemView.findViewById(R.id.date_text) as TextView }
        val peopleText by lazy { itemView.findViewById(R.id.people_text) as TextView }
        val statusImage by lazy { itemView.findViewById(R.id.status_image) as ImageView }

      //  val replyValue by lazy { itemView.findViewById(R.id.reply_value) as TextView }
        //val detailsValue by lazy { itemView.findViewById(R.id.details_value) as TextView }

      //  val confirmButton by lazy { itemView.findViewById(R.id.confirm_button) as Button }
        //val cancelButton by lazy { itemView.findViewById(R.id.cancel_button) as Button }
        //val buttonLayout by lazy { itemView.findViewById(R.id.button_layout) as RelativeLayout }
        //val replyLayout by lazy { itemView.findViewById(R.id.reply_layout) as LinearLayout }

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View) {
            reservClickListener.onItemClick(viewModel.getReservationAt(adapterPosition)!!,adapterPosition, v)
        }
    }

    interface ReservationClickListener {
        fun onItemClick(data: Data, position: Int, v: View)
    }

    interface StatusClickListener {
        fun onButtonClick(reserStatusRequest: ReserStatusRequest)
    }

    private fun openReplyPopUp(position: Int,orderStatus:String){
        try{
            MaterialDialog(mContext).show {
                title(R.string.reply)
                input(
                    hint = "Reply",
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    allowEmpty = true
                ) { _, text ->
                    //toast("Input: $text")
                    var replyString = ""
                    if(text.isNotEmpty())
                        replyString = text.toString()

                    callConfirmService(replyString,position,orderStatus)
                }
                positiveButton(R.string.reply)
                negativeButton(R.string.cancel_text)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}