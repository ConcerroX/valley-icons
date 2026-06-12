package concerrox.valley.iconpackmaker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import concerrox.ui.component.XButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconGridItem(
    item: concerrox.valley.iconpackmaker.model.IconItem,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = 4.dp, 
        modifier = modifier
            .size(80.dp)
            .aspectRatio(1f)
            .combinedClickable(
                onClick = { /* 普通点击不触发编辑 */ },
                onDoubleClick = onEditClick
            )
    ) {
        Column(
            modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标预览区域
            Box(
                modifier = Modifier.height(48.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconPreview(drawableName = item.drawable)
            }

            // 图标名称
            Text(
                text = item.drawable,
                style = MaterialTheme.typography.caption,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )

            // 组件信息（如果有）
            if (item.component.isNotBlank()) {
                Text(
                    text = item.component,
                    style = MaterialTheme.typography.overline,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            // 操作按钮行
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 编辑按钮
                XButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f).padding(end = 2.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text("编辑", style = MaterialTheme.typography.overline)
                }
                
                // 删除按钮
                XButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f).padding(start = 2.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text("删除", style = MaterialTheme.typography.overline)
                }
            }
        }
    }
}

@Composable
fun IconPreview(
    drawableName: String, modifier: Modifier = Modifier
) {
    val backgroundPath = "app/src/main/res/drawable-v26/${drawableName}_background.png"
    val foregroundPath = "app/src/main/res/drawable-v26/${drawableName}_foreground.png"

    Box(modifier = modifier.size(48.dp)) {
        // 背景图片
        ImageFromFile(
            filePath = backgroundPath,
            contentDescription = "背景图片",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // 前景图片（叠加在背景上）
        ImageFromFile(
            filePath = foregroundPath,
            contentDescription = "前景图片",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun ImageFromFile(
    filePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val projectRoot = concerrox.valley.iconpackmaker.util.FilePathManager.getProjectRoot()
    val fullFilePath = "$projectRoot/$filePath"
    val file = java.io.File(fullFilePath)
    
    // 使用LaunchedEffect在后台线程中安全地读取文件
    var imageData by remember { mutableStateOf<ByteArray?>(null) }
    var loadingError by remember { mutableStateOf(false) }
    
    LaunchedEffect(file) {
        if (file.exists() && file.extension.lowercase() == "png") {
            try {
                imageData = withContext(Dispatchers.IO) {
                    file.readBytes()
                }
            } catch (e: Exception) {
                loadingError = true
            }
        }
    }
    
    when {
        imageData != null -> {
            AsyncImage(
                model = imageData!!,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        loadingError -> {
            ErrorPlaceholder(modifier = modifier)
        }
        else -> {
            ErrorPlaceholder(modifier = modifier, isError = false)
        }
    }
}

@Composable
private fun ErrorPlaceholder(
    modifier: Modifier = Modifier,
    isError: Boolean = true
) {
    val backgroundColor = if (isError) {
        MaterialTheme.colors.error.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
    }
    
    val textColor = if (isError) {
        MaterialTheme.colors.error
    } else {
        MaterialTheme.colors.onSurface
    }
    
    val text = if (isError) "❗" else "❓"
    
    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            color = textColor
        )
    }
}

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun AddItemCard(
    categoryTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = 4.dp,
        onClick = onClick,
        modifier = modifier
            .size(80.dp)
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 添加图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary
                )
            }
            
            // 文字说明
            Text(
                text = "添加图标",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}