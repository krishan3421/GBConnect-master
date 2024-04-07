package com.gb.restaurant.di.module

import com.gb.restaurant.Constant
import com.gb.restaurant.api.GBClient
import com.gb.restaurant.api.HttpClientService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton


/**
 * Created by Krishan on 08/20/2019
 */
@Module
class ApiModule {
    @Provides @Singleton
    @Named("gbClient")
    fun provideApiService(): GBClient {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constant.GB_URL)
            .client(HttpClientService.getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
       // RetrofitHolder.retrofit = retrofit
      return retrofit.create(GBClient::class.java)
    }

    @Provides @Singleton
    @Named("gdClient")
    fun provideApiServiceNew(): GBClient {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constant.GD_URL)
            .client(HttpClientService.getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        //RetrofitHolder.retrofit = retrofit
        return retrofit.create(GBClient::class.java)
    }


}