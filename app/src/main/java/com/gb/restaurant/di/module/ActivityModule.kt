package com.gb.restaurant.di.module

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ActivityModule(private val activity: Activity) {

    @Provides @Singleton
   fun provideActivityContext(): Context {
        return activity
    }

    @Provides
    fun getResources(context:Context): Resources? {
        return context.resources
    }

}