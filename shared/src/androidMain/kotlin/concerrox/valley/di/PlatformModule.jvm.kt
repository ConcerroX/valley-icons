package concerrox.valley.di

import concerrox.valley.data.source.AndroidIconDrawableProvider
import concerrox.valley.data.source.AndroidXmlProvider
import concerrox.valley.data.source.IconDrawableProvider
import concerrox.valley.data.source.XmlProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {

    single<XmlProvider> {
        AndroidXmlProvider(androidContext())
    }

    single<IconDrawableProvider> {
        AndroidIconDrawableProvider(androidContext())
    }

}