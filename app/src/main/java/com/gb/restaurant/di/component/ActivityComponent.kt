package com.gb.restaurant.di.component

import com.gb.restaurant.di.module.ActivityModule
import dagger.Component
import javax.inject.Singleton

/**
 * Created by Krishan on 08/20/2019
 */

@Singleton
@Component(modules = arrayOf(
                ActivityModule::class
        )
)
interface ActivityComponent {
   // fun inject(loginActivity: LoginActivity)

}