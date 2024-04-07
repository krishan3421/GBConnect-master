package com.gb.restaurant.session

import android.content.Context
import android.content.SharedPreferences


class SessionManager(mContext: Context) {

    // Shared Preferences
    lateinit var pref: SharedPreferences

    // Editor for Shared preferences
    lateinit var editor: SharedPreferences.Editor

    companion object {
        private val PREF_NAME = "GB_RESTAURANT_PREF"

        // All Shared Preferences Keys
        private val IS_LOGIN = "IsLoggedIn"
        val USERNAME: String = "USERNAME"
        val KEY_FNAME = "KEY_FNAME"
        val KEY_LNAME = "KEY_LNAME"
        val KEY_FROMSOCIAL = "KEY_FROMSOCIAL"
        val PASSWORD: String = "Password"
        val ADDRESS_LIST = "ADDRESS_LIST"


        val PRINTER_ADDRESS = "printer_address"
        val PRINTER_TYPE = "printer_type"
        val API_TYPE = "GD"

    }

    init {
        pref = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = pref.edit()
    }

    /**
     * Create login session
     * */
    public fun createLoginSession(userName: String, password: String, isAutoLogin: Boolean,apiType:String="GD") {
        // Storing email in pref
        try {
            editor.putString(USERNAME, userName)
            editor.putString(PASSWORD, password)
            editor.putBoolean(IS_LOGIN, isAutoLogin)
            editor.putString(API_TYPE, apiType)
            // commit changes
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setAutoLoginStatus(isAutoLogin: Boolean) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, isAutoLogin)
        // commit changes
        editor.commit()
    }

    fun setSocialStatus(fromSocial: Boolean) {
        // Storing login value as TRUE
        editor.putBoolean(KEY_FROMSOCIAL, fromSocial)
        // commit changes
        editor.commit()
    }

    /**
     * Clear session details
     */
    fun logoutSharePref() {
        // Clearing all data from Shared Preferences
        editor.clear()
        editor.commit()
    }

    fun logout() {
        createLoginSession("", "", false)
    }

    fun updatePassword(pass: String) {
        editor.putString(PASSWORD, pass)
        editor.commit()
    }

    fun isAutoLoggedIn(): Boolean {
        return pref.getBoolean(IS_LOGIN, false)
    }

    fun isSocialLogin(): Boolean {
        return pref.getBoolean(KEY_FROMSOCIAL, false)
    }


    fun setPrinterAddress(address: String) {
        editor.putString(PRINTER_ADDRESS, address).commit()
    }

    fun getPrinterAddress(): String {
        return pref.getString(PRINTER_ADDRESS, "").toString()
    }

    fun setPrinterType(printerType: Int) {
        editor.putInt(PRINTER_TYPE, printerType).commit()
    }

    fun getPrinterType(): Int {
        return pref.getInt(PRINTER_TYPE, 0)
    }

    /**
     * Get stored session data
     */
    fun getUserDetails(): Map<String, String?> {
        val user = mutableMapOf<String, String?>()
        user[USERNAME] = pref.getString(USERNAME, "")
        user[PASSWORD] = pref.getString(PASSWORD, "")
        // return user
        return user
    }

    fun setApiType(apiType: String) {
        editor.putString(API_TYPE, apiType)
        editor.commit()
    }

    fun getApiType(): String {
        return pref.getString(API_TYPE, "GD")?:"GD"
    }
}