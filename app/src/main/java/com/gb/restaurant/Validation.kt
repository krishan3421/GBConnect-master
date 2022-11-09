package com.gb.restaurant

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import java.util.regex.Pattern

class Validation {

    companion object{
         val TAG = Validation::class.java.simpleName
         const val emailRegex = "(?:[a-z0-9!#$%\\&'*+/=?\\^_`{|}~-]+(?:\\.[a-z0-9!#$%\\&'*+/=?\\^_`{|}~-]+)" +
                "*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09" +
                "\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]" +
                "*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"

        /**
         * Static method to check whether the device is connected to any network
         *
         */
        fun isOnline(ctx: Context): Boolean {
            try {
                val cm = ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val netInfo = cm.activeNetworkInfo
                if (netInfo != null) {
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, e.message!!)
            }

            return false

        }

        @SuppressLint("DefaultLocale")
        fun validateEmail(email: String): Boolean {

            try {
                val emailLowerCase = email.toLowerCase()
                val r = Pattern.compile(emailRegex)

                val m = r.matcher(emailLowerCase)
                if (m.matches()) {
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, e.message!!)
            }

            return false
        }

         fun isValidMobile(phone: String): Boolean {
            var check = false
             check = if (!Pattern.matches("[a-zA-Z]+", phone)) {
                 !(phone.length < 10 || phone.length > 10)
             } else {
                 false
             }
            return check
        }

    }
}