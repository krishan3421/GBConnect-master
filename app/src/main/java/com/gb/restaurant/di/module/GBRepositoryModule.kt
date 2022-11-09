package com.gb.restaurant.di.module

import com.gb.restaurant.api.GBClient
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.di.GBRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Krishan on 08/20/2019
 */
@Module
class GBRepositoryModule {

    @Provides @Singleton
    fun providePostRepository(apiService: GBClient): GBRepository =
        GBRepositoryImpl(apiService)
}