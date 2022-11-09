package com.gb.restaurant.di.component

import com.gb.restaurant.di.module.ApiModule
import com.gb.restaurant.di.module.AppModule
import com.gb.restaurant.di.module.GBRepositoryModule
import com.gb.restaurant.viewmodel.*
import dagger.Component
import javax.inject.Singleton

/**
 * Created by Krishan on 08/20/2019
 */

@Singleton
@Component(
        modules = arrayOf(
                ApiModule::class,
                GBRepositoryModule::class,
                AppModule::class
        )
)
interface AppComponent {

        fun inject(rsLoginViewModel: RsLoginViewModel)
        fun inject(orderViewModel: OrderViewModel)
        fun inject(reportViewModel: ReportViewModel)
        fun inject(supportViewModel: SupportViewModel)
        fun inject(destinViewModel: DestinViewModel)
        fun inject(orderStatusViewModel: OrderStatusViewModel)
        fun inject(tipsViewModel: TipsViewModel)
        fun inject(resetPassViewModel: ResetPassViewModel)
        fun inject(reservationViewModel: ReservationViewModel)
        fun inject(addTipsViewModel: AddTipsViewModel)
        fun inject(couponViewModel: CouponViewModel)
        fun inject(bankViewModel: BankViewModel)
}