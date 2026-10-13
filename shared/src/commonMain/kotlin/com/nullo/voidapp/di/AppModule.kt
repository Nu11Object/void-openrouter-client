package com.nullo.voidapp.di

import org.koin.dsl.module
import kotlin.time.Clock

internal val appModule = module {
    single<Clock> { Clock.System }
}
