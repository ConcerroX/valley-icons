@file:Suppress("UsingMaterialAndMaterial3Libraries")

package concerrox.ui.component

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun XMaterialTheme(
    materialTheme: @Composable (@Composable () -> Unit) -> Unit = { MaterialTheme(content = it) },
    content: @Composable () -> Unit
) {
    materialTheme(content)
}