package com.gb.restaurant.di.module

import com.gb.restaurant.Constant
import com.gb.restaurant.api.GBClient
import com.gb.restaurant.api.HttpClientService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


/**
 * Created by Krishan on 08/20/2019
 */
@Module
class ApiModule {
    @Provides @Singleton
    fun provideApiService(): GBClient {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
                .baseUrl(Constant.WEBSERVICE.RESTSERVICEURL)
                .client(HttpClientService.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(GBClient::class.java)
    }


}