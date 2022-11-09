package com.gb.restaurant

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.gb.restaurant.di.ComponentInjector
import com.gb.restaurant.model.order.Data
import com.gb.restaurant.model.rslogin.RsLoginResponse
import com.gb.restaurant.utils.Utils
import net.posprinter.posprinterface.IMyBinder
import net.posprinter.service.PosprinterService

class MyApp : Application() {


    var rsLoginResponse: RsLoginResponse? = null
    var data: Data? = null
    var deviceToken: String = ""
    var isAlarm: Boolean = true

    companion object {

        val TAG = MyApp::class.java.simpleName
        lateinit var instance: MyApp
            private set

    }

    var mSerconnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Utils.myBinder = service as IMyBinder
            Log.e("myBinder", "connect")
            //getPrinterStatus();
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.e("myBinder", "disconnect")
        }
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        ComponentInjector.init(this)

        val intent = Intent(this, PosprinterService::class.java)
        bindService(intent, mSerconnection, BIND_AUTO_CREATE)
        Log.e("myBinder", "startMunbynService: ")
    }
}