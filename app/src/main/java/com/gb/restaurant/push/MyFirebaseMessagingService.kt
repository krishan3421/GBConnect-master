package com.gb.restaurant.push

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gb.restaurant.ui.EventEnquiryActivity
import com.gb.restaurant.ui.OrdersActivity
import com.gb.restaurant.ui.ReservationActivity
import com.gb.restaurant.utils.Util
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.time.Instant

class MyFirebaseMessagingService : FirebaseMessagingService(){
    companion object {
        private const val ADMIN_CHANNEL_ID = "admin_channel"
        const val PUSHBROADCAST = "com.gb.restaurant.push.MyFirebaseMessagingService"
        const val PUSHBROADCAST_RESERVATION = "com.gb.restaurant.push.MyFirebaseMessagingService.RESERVATION"
        const val PUSHBROADCAST_ENQUERY = "com.gb.restaurant.push.MyFirebaseMessagingService.ENQUERY"
        const val PUSH_KEY:String = "PUSH_KEY"
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("data>>>>>> ${Util.getStringFromBean(remoteMessage)}")

        var pushMessage = PushMessage()
        if(remoteMessage?.notification !=null){
            println("notification>>>> ")
            if(!remoteMessage?.notification?.title.isNullOrEmpty()){
                pushMessage.title =remoteMessage?.notification?.title!!
            }
            if(!remoteMessage?.notification?.body.isNullOrEmpty()){
                pushMessage.body =remoteMessage?.notification?.body!!
            }
        }
        if(remoteMessage?.data !=null){
            println("data>>>> ")
            if(remoteMessage?.data?.containsKey(MESSTYPE.title.toString())!!){
                pushMessage.title =remoteMessage?.data?.get(MESSTYPE.title.toString())!!
            }
            if(remoteMessage?.data?.containsKey(MESSTYPE.body.toString())!!){
                pushMessage.body =remoteMessage?.data?.get(MESSTYPE.body.toString())!!
            }
            if(remoteMessage?.data?.containsKey(MESSTYPE.type.toString())!!){
                pushMessage.type =remoteMessage?.data?.get(MESSTYPE.type.toString())!!
            }
            if(remoteMessage?.data?.containsKey(MESSTYPE.subtype.toString())!!){
                pushMessage.subtype =remoteMessage?.data?.get(MESSTYPE.subtype.toString())!!
            }
        }

        val foregroud: Boolean = ForegroundCheckTask().execute(applicationContext).get()
        if(!foregroud) {
            NotificationHelper(applicationContext).createNotification(pushMessage)
        }else{
            if(pushMessage.type.equals(TYPE.OrderNew.toString(),true) || pushMessage.type.equals(TYPE.OrderHold.toString(),true)){
                if(OrdersActivity.isPageVisible){
                    var intent =Intent(PUSHBROADCAST)
                    intent.putExtra(PUSH_KEY,pushMessage)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }else{
                    NotificationHelper(applicationContext).createNotification(pushMessage)
                }
            }else if(pushMessage.type.equals(TYPE.Reservation.toString(),true)){
                if(ReservationActivity.isReservationVisible){
                    var intent =Intent(PUSHBROADCAST_RESERVATION)
                    intent.putExtra(PUSH_KEY,pushMessage)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }else{
                    NotificationHelper(applicationContext).createNotification(pushMessage)
                }
            }else if(pushMessage.type.equals(TYPE.Inquiry.toString(),true)){
                if(EventEnquiryActivity.isEnqueryVisible){
                    var intent =Intent(PUSHBROADCAST_ENQUERY)
                    intent.putExtra(PUSH_KEY,pushMessage)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }else{
                    NotificationHelper(applicationContext).createNotification(pushMessage)
                }
            }else{
                NotificationHelper(applicationContext).createNotification(pushMessage)
            }
//            if(OrdersActivity.isPageVisible){
//                var intent =Intent(PUSHBROADCAST)
//                intent.putExtra(PUSH_KEY,pushMessage)
//                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
//            }else if(ReservationActivity.isReservationVisible){
//                var intent =Intent(PUSHBROADCAST_RESERVATION)
//                intent.putExtra(PUSH_KEY,pushMessage)
//                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
//            }else if(EventEnquiryActivity.isEnqueryVisible){
//                var intent =Intent(PUSHBROADCAST_ENQUERY)
//                intent.putExtra(PUSH_KEY,pushMessage)
//                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
//            }
//            else{
//                NotificationHelper(applicationContext).createNotification(pushMessage)
//            }

        }
    }
}