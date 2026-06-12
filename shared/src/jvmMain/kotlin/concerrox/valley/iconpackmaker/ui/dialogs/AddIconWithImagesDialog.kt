package concerrox.valley.iconpackmaker.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import concerrox.ui.component.XButton
import concerrox.valley.iconpackmaker.model.IconItem
import coil3.compose.AsyncImage
import java.io.File

// 验证组件信息格式是否正确
fun isValidComponentInfo(component: String): Boolean {
    if (component.isBlank()) return true // 可选字段
    
    // 检查基本格式: ComponentInfo{包名/Activity路径}
    return component.startsWith("ComponentInfo{") && 
           component.endsWith("}") &&
           component.contains("/") &&
           component.length > "ComponentInfo{}/".length
}

// 格式化组件信息（自动添加ComponentInfo包装）
fun formatComponentInfo(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return ""
    
    // 如果已经包含ComponentInfo包装，直接返回
    if (trimmed.startsWith("ComponentInfo{")) return trimmed
    
    // 如果是简单的包名/Activity格式，自动包装
    if (trimmed.contains("/") && !trimmed.contains("{")) {
        return "ComponentInfo{$trimmed}"
    }
    
    return trimmed
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddIconWithImagesDialog(
    categoryTitle: String,
    itemToEdit: IconItem? = null, // 编辑模式时传入现有项
    onDismiss: () -> Unit, 
    onConfirm: (String, String, File?, File?) -> Unit
) {
    val prefix = if (itemToEdit == null) categoryTitle.lowercase() + "_" else ""
    var drawableName by remember(prefix) { mutableStateOf(if (itemToEdit != null) itemToEdit.drawable else "") }
    var componentName by remember { mutableStateOf(if (itemToEdit != null) itemToEdit.component else "") }
    var foregroundImage by remember { mutableStateOf<File?>(null) }
    var backgroundImage by remember { mutableStateOf<File?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (itemToEdit == null) { // 只有添加模式才自动聚焦
            focusRequester.requestFocus()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (itemToEdit != null) "编辑图标 - ${itemToEdit.drawable}" 
                else "向 '$categoryTitle' 添加图标"
            ) 
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Drawable名称输入（编辑模式下只读）
                if (itemToEdit == null) {
                    Text(
                        text = "Drawable名称将自动添加前缀: $prefix",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = drawableName,
                        onValueChange = { drawableName = it },
                        label = { Text("Drawable 名称") },
                        prefix = { Text(prefix) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                } else {
                    OutlinedTextField(
                        value = drawableName,
                        onValueChange = {},
                        label = { Text("Drawable 名称") },
                        enabled = false, // 编辑模式下不可修改
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = componentName,
                    onValueChange = { 
                        // 实时格式化输入
                        val formatted = formatComponentInfo(it)
                        componentName = formatted
                    },
                    label = { Text("组件信息 (可选)") },
                    placeholder = { Text("例如: com.example/com.example.MainActivity") },
                    isError = componentName.isNotBlank() && !isValidComponentInfo(componentName),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (componentName.isNotBlank() && !isValidComponentInfo(componentName)) {
                        "格式错误：请输入正确的组件信息格式"
                    } else {
                        "用于关联应用组件，支持自动格式化。示例：com.example/com.example.MainActivity"
                    },
                    style = MaterialTheme.typography.caption,
                    color = if (componentName.isNotBlank() && !isValidComponentInfo(componentName)) {
                        MaterialTheme.colors.error
                    } else {
                        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 图片选择区域
                Text(
                    text = "图片选择",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 前景图片区域
                ImageDropArea(
                    label = "前景图片 (Foreground)",
                    currentImage = foregroundImage,
                    onImageSelected = { foregroundImage = it },
                    isDragging = isDragging && foregroundImage == null
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 背景图片区域
                ImageDropArea(
                    label = "背景图片 (Background)",
                    currentImage = backgroundImage,
                    onImageSelected = { backgroundImage = it },
                    isDragging = isDragging && backgroundImage == null
                )

                // 当前图片预览（仅编辑模式）
                if (itemToEdit != null) {
                    CurrentImagesPreview(item = itemToEdit)
                }

                // 预览区域
                if (foregroundImage != null || backgroundImage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "预览",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    IconPreviewArea(
                        foregroundImage = foregroundImage,
                        backgroundImage = backgroundImage,
                        drawableName = if (drawableName.isNotBlank()) prefix + drawableName else ""
                    )
                }
            }
        },
        confirmButton = {
            XButton(
                onClick = {
                    val finalDrawableName = if (itemToEdit != null) drawableName else prefix + drawableName
                    onConfirm(finalDrawableName, componentName, foregroundImage, backgroundImage)
                },
                enabled = if (itemToEdit != null) true else drawableName.isNotBlank() && (foregroundImage != null || backgroundImage != null)
            ) {
                Text(if (itemToEdit != null) "更新" else "创建")
            }
        },
        dismissButton = {
            XButton(onClick = onDismiss) {
                Text("取消")
            }
        })
}

// 处理拖放事件
@OptIn(ExperimentalComposeUiApi::class)
private fun handleDropEvent(
    event: androidx.compose.ui.draganddrop.DragAndDropEvent, onImageSelected: (File) -> Unit
): Boolean {
    return try {
        when (val dragData = event.dragData()) {
            is androidx.compose.ui.draganddrop.DragData.FilesList -> {
                handleFilesListDrop(dragData, onImageSelected)
            }

            is androidx.compose.ui.draganddrop.DragData.Text -> {
                handleTextDrop(dragData, onImageSelected)
            }

            else -> {
                handleFallbackDrop(dragData, onImageSelected)
            }
        }
    } catch (_: Exception) {
        false
    }
}

// 处理文件列表拖放
@OptIn(ExperimentalComposeUiApi::class)
private fun handleFilesListDrop(
    filesList: androidx.compose.ui.draganddrop.DragData.FilesList, onImageSelected: (File) -> Unit
): Boolean {
    val files = filesList.readFiles()
    return files.any { filePath ->
        try {
            val cleanPath = cleanFilePath(filePath)
            val file = File(cleanPath)
            if (isValidImageFile(file)) {
                onImageSelected(file)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}

// 处理文本拖放
@OptIn(ExperimentalComposeUiApi::class)
private fun handleTextDrop(
    textData: androidx.compose.ui.draganddrop.DragData.Text, onImageSelected: (File) -> Unit
): Boolean {
    val text = textData.readText()

    // 尝试多种路径清理方式
    val cleanPaths = listOf(
        text.trim(),
        text.trim().removeSurrounding("\""),
        text.trim().removeSurrounding("'"),
        text.trim().replace("\\", "/")
    ).distinct()

    return cleanPaths.any { cleanPath ->
        if (cleanPath.isNotEmpty()) {
            try {
                val file = File(cleanPath)
                if (isValidImageFile(file)) {
                    onImageSelected(file)
                    true
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        } else {
            false
        }
    }
}

// 处理后备方案
@OptIn(ExperimentalComposeUiApi::class)
private fun handleFallbackDrop(
    dragData: Any, onImageSelected: (File) -> Unit
): Boolean {
    try {
        val textData = dragData as? androidx.compose.ui.draganddrop.DragData.Text
        if (textData != null) {
            val text = textData.readText()
            val file = File(text.trim())
            if (isValidImageFile(file)) {
                onImageSelected(file)
                return true
            }
        }
    } catch (_: Exception) {
        // 静默处理异常
    }
    return false
}

// 清理文件路径，移除协议前缀和其他特殊字符
private fun cleanFilePath(filePath: String): String {
    var cleanPath = filePath.trim()

    // 移除file:协议前缀
    if (cleanPath.startsWith("file:/", ignoreCase = true)) {
        cleanPath = cleanPath.substring(6) // 移除"file:/"
    }

    // 处理可能的双斜杠
    cleanPath = cleanPath.replace("//", "/")

    // Windows路径处理：如果以/开头但第二个字符是字母，可能是错误的路径格式
    if (cleanPath.startsWith("/") && cleanPath.length > 2 && cleanPath[1].isLetter() && cleanPath[2] == ':') {
        cleanPath = cleanPath.substring(1)
    }

    // 移除可能的引号
    cleanPath = cleanPath.removeSurrounding("\"").removeSurrounding("'")

    return cleanPath
}

// 验证是否为有效的图片文件
private fun isValidImageFile(file: File): Boolean {
    return file.exists() && file.isFile && file.extension.lowercase() in listOf(
        "png", "jpg", "jpeg", "webp", "gif", "bmp"
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ImageDropArea(
    label: String,
    currentImage: File?,
    onImageSelected: (File) -> Unit,
    isDragging: Boolean,
    modifier: Modifier = Modifier
) {
    var dragState by remember { mutableStateOf(isDragging) }

    val dropTarget = remember {
        object : androidx.compose.ui.draganddrop.DragAndDropTarget {
            override fun onDrop(event: androidx.compose.ui.draganddrop.DragAndDropEvent): Boolean {
                dragState = false
                return handleDropEvent(event, onImageSelected)
            }

            override fun onEntered(event: androidx.compose.ui.draganddrop.DragAndDropEvent) {
                dragState = true
            }

            override fun onExited(event: androidx.compose.ui.draganddrop.DragAndDropEvent) {
                dragState = false
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().border(
            border = BorderStroke(
                width = 2.dp, color = if (dragState) MaterialTheme.colors.primary
                else if (currentImage != null) Color.Green
                else MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
            ), shape = RoundedCornerShape(8.dp)
        ).padding(16.dp).dragAndDropTarget(
            shouldStartDragAndDrop = { true },
            target = dropTarget,
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (currentImage != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentImage.name,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.weight(1f)
                )
                XButton(
                    onClick = { onImageSelected(File("")) }, // Clear selection
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("清除", fontSize = 12.sp)
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (dragState) "📁 释放文件以选择" else "📁 拖放图片文件到这里",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "支持 PNG, JPG, JPEG, WEBP, GIF, BMP 格式",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun IconPreviewArea(
    foregroundImage: File?,
    backgroundImage: File?,
    drawableName: String,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = 4.dp, modifier = modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标预览
            Box(
                modifier = Modifier.size(96.dp).border(
                    border = BorderStroke(
                        1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                    ), shape = RoundedCornerShape(20.dp)
                ).padding(8.dp), contentAlignment = Alignment.Center
            ) {
                // 背景预览
                if (backgroundImage != null && backgroundImage.exists()) {
                    AsyncImage(
                        model = backgroundImage,
                        contentDescription = "背景预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }

                // 前景预览
                if (foregroundImage != null && foregroundImage.exists()) {
                    AsyncImage(
                        model = foregroundImage,
                        contentDescription = "前景预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 信息显示
            if (drawableName.isNotBlank()) {
                Text(
                    text = "将创建: $drawableName",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }

            if (backgroundImage != null) {
                Text(
                    text = "背景: ${backgroundImage.name}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }

            if (foregroundImage != null) {
                Text(
                    text = "前景: ${foregroundImage.name}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CurrentImagesPreview(
    item: IconItem,
    modifier: Modifier = Modifier
) {
    val backgroundPath = "app/src/main/res/drawable-v26/${item.drawable}_background.png"
    val foregroundPath = "app/src/main/res/drawable-v26/${item.drawable}_foreground.png"
    
    val projectRoot = System.getProperty("user.dir")
    val backgroundFile = File("$projectRoot/$backgroundPath")
    val foregroundFile = File("$projectRoot/$foregroundPath")
    
    val hasCurrentImages = backgroundFile.exists() || foregroundFile.exists()
    
    if (hasCurrentImages) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "当前图片",
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            elevation = 4.dp, 
            modifier = modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 当前图标预览
                Box(
                    modifier = Modifier.size(96.dp).border(
                        border = BorderStroke(
                            1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                        ), shape = RoundedCornerShape(20.dp)
                    ).padding(8.dp), contentAlignment = Alignment.Center
                ) {
                    // 背景预览
                    if (backgroundFile.exists()) {
                        AsyncImage(
                            model = backgroundFile,
                            contentDescription = "当前背景",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }

                    // 前景预览
                    if (foregroundFile.exists()) {
                        AsyncImage(
                            model = foregroundFile,
                            contentDescription = "当前前景",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 当前文件信息
                Text(
                    text = "当前图标: ${item.drawable}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )

                if (backgroundFile.exists()) {
                    Text(
                        text = "背景: ${backgroundFile.name}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }

                if (foregroundFile.exists()) {
                    Text(
                        text = "前景: ${foregroundFile.name}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}