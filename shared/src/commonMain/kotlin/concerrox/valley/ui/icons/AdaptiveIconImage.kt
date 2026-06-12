package concerrox.valley.ui.icons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import concerrox.valley.data.source.IconDrawableProvider
import org.koin.compose.koinInject

@Composable
fun AdaptiveIconImage(drawable: String, modifier: Modifier = Modifier) {
    val drawableProvider: IconDrawableProvider = koinInject()
    val foreground = remember(drawable) { drawableProvider.getDrawable(drawable + "_foreground") }
    val background = remember(drawable) { drawableProvider.getDrawable(drawable + "_background") }

    val targetScale = 432f / 288f
    val imageSize = (56 * targetScale).dp
    val shape = RoundedCornerShape(102.dp)
    Box(
        modifier = modifier.size(56.dp).aspectRatio(1f).shadow(2.dp, shape).clip(shape),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = background,
            contentDescription = null,
            modifier = Modifier.requiredSize(imageSize),
            contentScale = ContentScale.FillBounds,
        )
        AsyncImage(
            model = foreground,
            contentDescription = null,
            modifier = Modifier.requiredSize(imageSize),
            contentScale = ContentScale.FillBounds,
        )
    }
}