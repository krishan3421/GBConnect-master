package com.gb.restaurant.utils

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.afollestad.date.month
import com.afollestad.materialdialogs.MaterialDialog
import com.gb.restaurant.R
import com.google.gson.Gson
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class Util {

    companion object{
        val TAG= Util::class.java.simpleName
        var df: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a") as DateFormat
        var dAM: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa")
        var wsDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var wsssDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var mmddyyyyDateFormat = SimpleDateFormat("MM/dd/yyyy")
        var wssDateFormat = SimpleDateFormat("yyyyMMdd")
        var wssTimeFormat = SimpleDateFormat("EEEE, MMM dd, yyyy hh:mm:ss aa")
        var wsssTimeFormat = SimpleDateFormat("EEEE, MMM dd, yyyy")
        var yyyy_mm_yy: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        var orderScheduleFormat = SimpleDateFormat("MMM dd, EEE")
        var mm_dd_yyyy = SimpleDateFormat("MMM dd yyyy")
        var MM_dd_yyyy = SimpleDateFormat("MMM dd, yyyy")
        var isShowing = false


         fun alertDialog(message:String, context: Context) {
            try {
                if (!isShowing) {
                    val builder1 = AlertDialog.Builder(context)
                    builder1.setTitle(context.getString(R.string.app_name_alert))
                    builder1.setMessage(message)
                    builder1.setCancelable(false)
                    builder1.setPositiveButton(context.getString(R.string.ok)) { dialog, id ->
                         isShowing = false
                        dialog.cancel()
                    }.show()
                     isShowing = true
                 }
            }catch (e:Exception) {
                e.printStackTrace()
                Log.e(TAG, e.message!!)
            }
         }

        fun alert(message:String,context: Context){
            try {
                MaterialDialog(context).show {
                    title(R.string.app_name_alert)
                    message(null,message)
                    positiveButton {

                    }
                    positiveButton(R.string.ok)

                }
            }catch (e:Exception){
                e.printStackTrace()
                Log.e(TAG, e.message!!)
            }
        }

        fun getStringFromBean(bean: Any): String? {
            var result: String? = null
            try {
                result = Gson().toJson(bean)
            } catch (e: Exception) {
                Log.e(TAG, e.message!!)
                e.printStackTrace()
            }
            return result

        }

        fun getCurrentDate(): String? {
            var currDate: String? = null
            var calendar: Calendar? = null
            var df: SimpleDateFormat? = null
            try {
                calendar = Calendar.getInstance()
                df = SimpleDateFormat("dd/MM/yyyy")
                currDate = df.format(calendar!!.time)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return currDate
        }
        fun getCurrentDateHome(): String? {
            var currDate: String? = null
            var calendar: Calendar? = null
            var df: SimpleDateFormat? = null
            try {
                calendar = Calendar.getInstance()
                df = SimpleDateFormat("dd MMM yyyy")
                currDate = df.format(calendar!!.time)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return currDate
        }
        fun getYYYY_MM_DD(): String? {
            var currDate: String? = null
            var calendar: Calendar? = null
            try {
                calendar = Calendar.getInstance()
                currDate = wsssDateFormat.format(calendar!!.time)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return currDate
        }
        fun getMM_day_yyyy(date:Date): String? {
            var currDate: String? = null
            try {
                currDate = mm_dd_yyyy.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return currDate
        }
        fun getYYYYMMDD(): String? {
            var currDate: String? = null
            var calendar: Calendar? = null
            try {
                calendar = Calendar.getInstance()
                currDate = wssDateFormat.format(calendar!!.time)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return currDate
        }
        fun getyyyy_mm_dd_hh_mm_ss(date:Date): String? {
            var currDateTime: String? = null
            try {
                currDateTime = wsDateFormat.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return currDateTime
        }
        fun getMMM_DD_YYYY(date:Date): String? {
            var currDateTime: String? = null
            try {
                currDateTime = MM_dd_yyyy.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return currDateTime
        }

        fun convertMiliSecToDate(myDate:Long):String?{
            var date = ""
            try{
                date= wssTimeFormat.format(Date(myDate))
             }catch (e:Exception){
                 e.printStackTrace()
                 Log.e(TAG,e.message!!)
             }
            return date
        }

        fun getCurrentTime(): String? {
            var currTime: String? = null
            var calendar: Calendar? = null
            try {
                calendar = Calendar.getInstance()
                val minute = calendar!!.get(Calendar.MINUTE)
                val hours = calendar.get(Calendar.HOUR_OF_DAY)
                currTime = "$hours:$minute"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return currTime
        }
        fun getCurrentMonth(): String? {
            var currMonth: String? = null
            var calendar: Calendar? = null
            try {
                calendar = Calendar.getInstance()
                val month = calendar!!.get(Calendar.MONTH)
                val monthText = twoDecimalValue(month+1)
                currMonth = "$monthText"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return currMonth
        }
        fun getCurrentYear(): String? {
            var currYear: String? = null
            var calendar: Calendar? = null
            try {
                calendar = Calendar.getInstance()
                val year = calendar!!.get(Calendar.YEAR)
                val yearText = twoDecimalValue(year)
                currYear = "$yearText"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return currYear
        }
        fun getCurrentYearInt(): Int? {
            var currYear: Int? = null
            var calendar: Calendar? = null
            try {
                calendar = Calendar.getInstance()
                 currYear = calendar!!.get(Calendar.YEAR)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return currYear
        }


        fun getTwoDigit(num:Float):Float{
            var floatValue =0.0F
            try{
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.CEILING
                floatValue = df.format(num).toFloat()
            }catch (e:Exception){
                e.printStackTrace()
                Log.e(TAG,e.message!!)
            }
            return  floatValue
        }
        fun latlongFormat(value: Double): String? {
            var df: DecimalFormat? = null
            var result: String? = null
            try {
                df = DecimalFormat("#.##########")
                df.roundingMode = RoundingMode.CEILING
                result = df.format(value)
            } catch (ex: Exception) {
                ex.printStackTrace()
                result = value.toString()
            }

            return result
        }

        fun getSelectedDateWithTime(c:Calendar): String? {
            var currTime: String? = null
            try {
                val year = c!!.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)+1
                val day = c.get(Calendar.DATE)
                val hours = c.get(Calendar.HOUR_OF_DAY)
                val minutes = c.get(Calendar.MINUTE)
                val seconds = c.get(Calendar.SECOND)

                val monthText =   twoDecimalValue(month)
                val dayText =   twoDecimalValue(day)
                val hoursText = twoDecimalValue(hours)
                val minuteText = twoDecimalValue(minutes)
                val secondsText = twoDecimalValue(seconds)
               // currTime = "$dayText-$monthText-$year $hoursText:$minuteText:$secondsText"
                currTime = "$year-$monthText-$dayText"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return currTime
        }
         fun getMonth():String{
             return try{
                 val formatter = SimpleDateFormat("MMM")
                 formatter.format(Date())

             }catch (e:Exception){
                 e.printStackTrace()
                 ""
             }
        }

        fun get_mmddyyyyString(c: Calendar): String? {
            return mmddyyyyDateFormat.format(c.time)
        }
        fun get_yyyy_mm_dd(c: Calendar): String? {
            return wsssDateFormat.format(c.time)
        }

        fun twoDecimalValue(value:Int):String{
            val formatter = DecimalFormat("00")
            var data = formatter.format(value)
            return formatter.format(value)
        }

        fun getVersionName(context:Context):String{
            return try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)

                pInfo.versionName
                //BuildConfig.VERSION_NAME
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                "1.0"
            }

        }
        fun getPackageName(context:Context):String{
            try {
                val pInfo = context.getPackageManager().getPackageInfo(context.packageName, 0)
                val packageName = pInfo.packageName
                return packageName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                return ""
            }

        }
        fun currentMonthStartDate(): Calendar{
            val calendar: Calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 0)
            calendar[Calendar.DATE] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
            return calendar
        }

        fun currentMonthEndDate(): Calendar{
            val calendar: Calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 0)
            calendar[Calendar.DATE] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            return calendar
        }

        fun getSelMonthEndDate(mcalendar: Calendar): Calendar{
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH,mcalendar.month)
            calendar[Calendar.DATE] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            return calendar
        }
    }

}