package concerrox.valley.ui.navigation

import androidx.navigation3.runtime.NavKey
import concerrox.valley.Res
import concerrox.valley.outline_dashboard_24
import org.jetbrains.compose.resources.DrawableResource

sealed interface Route : NavKey {

    val title: String

    sealed interface Main : Route {

        val iconRes: DrawableResource

        data object Dashboard : Main {
            override val title = "Dashboard"
            override val iconRes = Res.drawable.outline_dashboard_24
        }

        data object Icons : Main {
            override val title = "Icons"
            override val iconRes = Res.drawable.outline_dashboard_24
        }

    }

}