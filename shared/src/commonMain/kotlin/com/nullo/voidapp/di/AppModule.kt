package com.nullo.voidapp.di

import kotlin.time.Clock
import org.koin.dsl.module

internal val appModule = module {
    single<Clock> { Clock.System }
}
