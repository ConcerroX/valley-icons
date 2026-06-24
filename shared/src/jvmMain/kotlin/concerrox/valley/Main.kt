package concerrox.valley

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import concerrox.valley.di.dataModule
import concerrox.valley.di.platformModule
import concerrox.valley.di.uiModule
import concerrox.valley.iconpackmaker.IconPackMakerApp
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext.startKoin

fun main() = application {

    startKoin {
        modules(dataModule, uiModule, platformModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
    ) {
        App()
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
    ) {
        IconPackMakerApp()
    }

}