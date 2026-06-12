package concerrox.valley

import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import concerrox.ui.component.XMaterialTheme
import concerrox.valley.ui.icons.IconsPage
import concerrox.valley.ui.icons.IconsViewModel
import concerrox.valley.ui.navigation.Route
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    val backStack = remember { mutableStateListOf<Route>(Route.Main.Icons) }

    XMaterialTheme {
        val scaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_name)) },
                )
            },
            drawerContent = {
                AppDrawer(backStack.lastOrNull(), onNavigate = {
                    backStack.removeLastOrNull()
                    backStack.add(it)
                    scope.launch { scaffoldState.drawerState.close() }
                })
            },
            backgroundColor = Color.Transparent
        ) { contentPadding ->
            NavDisplay(
                modifier = Modifier.padding(contentPadding),
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = { key ->
                    when (key) {
                        is Route.Main.Dashboard -> NavEntry(key) { Text("Dashboard") }
                        is Route.Main.Icons -> NavEntry(key) {
                            val viewModel: IconsViewModel = koinViewModel()
                            val state by viewModel.uiState.collectAsState()
                            IconsPage(state)
                        }
                    }
                })
        }
    }
}

@Composable
fun AppDrawer(currentRoute: Route?, onNavigate: (Route) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        val routes = listOf(Route.Main.Dashboard, Route.Main.Icons)
        for (route in routes) {
            AppDrawerListItem(route, currentRoute == route, onNavigate)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppDrawerListItem(route: Route.Main, isSelected: Boolean, onNavigate: (Route) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier.fillMaxWidth().height(48.dp).clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { onNavigate(route) },
        ).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val shape = RoundedCornerShape(4.dp)
        Surface(
            modifier = Modifier.fillMaxWidth().clip(shape).indication(
                interactionSource = interactionSource, indication = ripple()
            ),
            color = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent,
            shape = shape,
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(route.iconRes),
                    contentDescription = route.title,
                    tint = if (isSelected) {
                        MaterialTheme.colors.primary
                    } else {
                        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    }
                )
                Spacer(Modifier.width(24.dp))
                Text(
                    text = route.title,
                    style = MaterialTheme.typography.subtitle2,
                    color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
