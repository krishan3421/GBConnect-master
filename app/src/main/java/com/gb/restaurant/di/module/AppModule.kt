package com.gb.restaurant.di.module

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.gb.restaurant.MyApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: MyApp) {

    @Provides @Singleton
   fun provideAppContext(): Context {
        return application
    }
    @Provides @Singleton
    fun provideSharedPref(context:Context):SharedPreferences{
        return context.getSharedPreferences("GBConnectPref",Context.MODE_PRIVATE)
    }

    @Provides
    fun getResources(context:Context): Resources? {
        return context.resources
    }

}