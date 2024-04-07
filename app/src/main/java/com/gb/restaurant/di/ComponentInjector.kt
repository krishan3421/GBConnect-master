package com.gb.restaurant.di

import com.gb.restaurant.MyApp
import com.gb.restaurant.di.component.AppComponent
import com.gb.restaurant.di.component.DaggerAppComponent
import com.gb.restaurant.di.module.ApiModule
import com.gb.restaurant.di.module.AppModule
import com.gb.restaurant.di.module.GBRepositoryModule


class ComponentInjector {

    companion object {

        lateinit var component: AppComponent

        fun init(application: MyApp) {
            component = DaggerAppComponent.builder()
                    .apiModule(ApiModule())
                    .appModule(AppModule(application))
                    .gBRepositoryModule(GBRepositoryModule())
                    .build()
        }
    }
}

