package concerrox.valley.ui.icons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import concerrox.valley.data.model.Icon
import concerrox.valley.data.model.IconCategory

@Composable
fun IconsPage(state: IconsUIState) {
    when (state) {
        is IconsUIState.Loading -> CircularProgressIndicator()
        is IconsUIState.Success -> IconsContentGrid(state.categories)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IconsContentGrid(categories: List<IconCategory>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.widthIn(max = (80 * 5).dp).fillMaxSize()
        ) {
            categories.forEach { category ->
                item(key = category.title + "_title", span = { GridItemSpan(5) }) {
                    ListItem {
                        Text(
                            text = category.title,
                            color = MaterialTheme.colors.secondaryVariant,
                            style = MaterialTheme.typography.h6
                        )
                    }
                }

                items(
                    items = category.items,
                    key = { icon -> icon.drawable }
                ) { icon ->
                    Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                        IconItem(icon)
                    }
                }
            }
        }
    }
}

@Composable
fun IconItem(icon: Icon) {
    Column(
        modifier = Modifier.width(80.dp).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false),
            onClick = {},
        ).padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AdaptiveIconImage(drawable = icon.drawable)
        Spacer(Modifier.height(4.dp))
        Text(
            text = icon.name,
            color = Color(0xFF5F6368),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            lineHeight = 18.sp
        )
    }
}