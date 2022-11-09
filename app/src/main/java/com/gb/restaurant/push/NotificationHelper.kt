package com.gb.restaurant.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gb.restaurant.MyApp
import com.gb.restaurant.R
import com.gb.restaurant.ui.HomeActivity
import com.gb.restaurant.ui.RestaurantLoginActivity
import com.gb.restaurant.ui.SplashActivity


class NotificationHelper(val mContext: Context) {

    private var mNotificationManager: NotificationManager? = null
    private var mBuilder: NotificationCompat.Builder? = null


        companion object{
            const val FROMNOTIFICATION:String = "FROMNOTIFICATION"
           const val PUSH_KEY:String = "PUSH_KEY"
            const val NOTIFICATION_CHANNEL_ID = "10001"
             var NOTIFICATION__ID = 0
            fun clearNotification(){

            }
        }
    /**
     * Create and push the notification
     */
    fun createNotification(pushMessage: PushMessage) {
        /**Creates an explicit intent for an Activity in your app */
        NOTIFICATION__ID++
        val resultIntent =  if(MyApp.instance.rsLoginResponse !=null) {
            Intent(mContext, HomeActivity::class.java)
        }else{
            Intent(mContext, SplashActivity::class.java)
        }
        resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val todoBundle = Bundle()
        todoBundle.putParcelable(PUSH_KEY, pushMessage)
        todoBundle.putBoolean(FROMNOTIFICATION,true)
        resultIntent.putExtra(PUSH_KEY,todoBundle)
        println("notification>>>>> ${pushMessage.title} ${pushMessage.body}")
        val resultPendingIntent = PendingIntent.getActivity(
            mContext,
            0 /* Request code */, resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )


        val bigText =NotificationCompat.BigTextStyle()
        bigText.bigText("${pushMessage.body}")
        mBuilder = NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)
        mBuilder?.setSmallIcon(R.mipmap.ic_launcher)
        mBuilder?.setContentTitle(pushMessage.title)!!
            //.setContentText(pushMessage.body)
            .setAutoCancel(true)
            .setSound(DEFAULT_NOTIFICATION_URI)
            .setStyle(bigText)
            .setColor(ContextCompat.getColor(mContext, R.color.colorAccent))
            //.addAction(R.drawable.ic_action_in,mContext.getString(R.string.archive),null)
            //.addAction(R.drawable.ic_action_in,mContext.getString(R.string.reply),null)
            .setContentIntent(resultPendingIntent).priority = NotificationManager.IMPORTANCE_HIGH

        mNotificationManager =
            mContext?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "NOTIFICATION_CHANNEL_NAME",
                importance
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            assert(mNotificationManager != null)
            mBuilder?.setChannelId(NOTIFICATION_CHANNEL_ID)

            mNotificationManager?.createNotificationChannel(notificationChannel)
        }
        assert(mNotificationManager != null)
        mNotificationManager?.notify(NOTIFICATION__ID /* Request Code */, mBuilder?.build())
    }
}