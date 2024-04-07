package com.gb.restaurant.di.module

import android.content.Context
import com.gb.restaurant.api.GBClient
import com.gb.restaurant.di.GBRepository
import com.gb.restaurant.di.GBRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Krishan on 08/20/2019
 */
@Module
class GBRepositoryModule {

    @Provides
    fun providePostRepository(@Named("gbClient") gbClient: GBClient,@Named("gdClient") gdClient: GBClient,context: Context): GBRepository =
        GBRepositoryImpl(gbClient,gdClient,context)
}