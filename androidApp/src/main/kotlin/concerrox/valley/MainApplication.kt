package concerrox.valley

import android.app.Application
import concerrox.valley.di.dataModule
import concerrox.valley.di.platformModule
import concerrox.valley.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(dataModule, uiModule, platformModule)
        }
    }
}