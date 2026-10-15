package com.mutissx.ruleplay

import android.app.Application
import com.mutissx.ruleplay.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RulePlayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RulePlayApp)
            modules(appModules)
        }
    }
}
