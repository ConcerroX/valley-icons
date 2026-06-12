package concerrox.valley.di

import concerrox.valley.ui.icons.IconsViewModel
import org.koin.dsl.module

val uiModule = module {
    factory { IconsViewModel(get()) }
}