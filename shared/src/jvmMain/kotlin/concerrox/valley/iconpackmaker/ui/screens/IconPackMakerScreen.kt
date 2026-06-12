package concerrox.valley.iconpackmaker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import concerrox.ui.component.XMaterialTheme
import concerrox.valley.iconpackmaker.ui.components.AddItemCard
import concerrox.valley.iconpackmaker.ui.components.IconGridItem
import concerrox.valley.iconpackmaker.ui.dialogs.AddCategoryDialog
import concerrox.valley.iconpackmaker.ui.dialogs.AddIconWithImagesDialog
import concerrox.valley.iconpackmaker.viewmodel.IconPackEditorViewModel
import kotlinx.coroutines.launch

@Composable
fun IconPackMakerScreen() {
    XMaterialTheme {
        val viewModelFactory = viewModelFactory { initializer { IconPackEditorViewModel() } }
        val viewModel: IconPackEditorViewModel = viewModel(factory = viewModelFactory)

        val uiState by viewModel.uiState.collectAsState()
        val state = uiState.iconPackData
        val isLoading = uiState.isLoading

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        var showAddCategoryDialog by remember { mutableStateOf(false) }
        var showAddIconDialog by remember { mutableStateOf(false) }
        var selectedCategoryTitle by remember { mutableStateOf("") }
        var selectedItem by remember { mutableStateOf<concerrox.valley.iconpackmaker.model.IconItem?>(null) }

        LaunchedEffect(Unit) {
            viewModel.loadXmlFiles()
        }

        LaunchedEffect(uiState.errorMessage) {
            uiState.errorMessage?.let {
                scope.launch {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearMessages()
                }
            }
        }

        LaunchedEffect(uiState.successMessage) {
            uiState.successMessage?.let {
                scope.launch {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearMessages()
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Valley 图标包编辑器") }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddCategoryDialog = true }) { 
                    Text("+") 
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            if (isLoading) {
                LoadingScreen(modifier = Modifier.fillMaxSize().padding(paddingValues))
            } else {
                MainContent(
                    categories = state.categories,
                    onAddIconClick = { categoryTitle ->
                        selectedCategoryTitle = categoryTitle
                        selectedItem = null
                        showAddIconDialog = true
                    },
                    onDeleteIconClick = { categoryTitle, itemId ->
                        viewModel.removeIconItem(categoryTitle, itemId)
                    },
                    onEditIconClick = { categoryTitle, item ->
                        selectedCategoryTitle = categoryTitle
                        selectedItem = item
                        showAddIconDialog = true
                    },
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                )
            }
        }

        // 添加分类对话框
        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false }, 
                onConfirm = { title ->
                    viewModel.addCategory(title)
                    showAddCategoryDialog = false
                }
            )
        }

        // 图标对话框（添加或编辑）
        if (showAddIconDialog && selectedCategoryTitle.isNotEmpty()) {
            AddIconWithImagesDialog(
                categoryTitle = selectedCategoryTitle,
                itemToEdit = selectedItem, // 编辑模式时传入现有项
                onDismiss = {
                    showAddIconDialog = false
                    selectedCategoryTitle = ""
                    selectedItem = null
                },
                onConfirm = { drawableName, component, foregroundImage, backgroundImage ->
                    if (selectedItem != null) {
                        // 编辑模式
                        viewModel.editIconItem(
                            categoryTitle = selectedCategoryTitle,
                            itemId = selectedItem!!.id,
                            componentName = component,
                            foregroundImage = foregroundImage,
                            backgroundImage = backgroundImage
                        )
                    } else {
                        // 添加模式
                        viewModel.addIconItemWithImages(
                            categoryTitle = selectedCategoryTitle,
                            drawableName = drawableName,
                            componentName = component,
                            foregroundImage = foregroundImage,
                            backgroundImage = backgroundImage
                        )
                    }
                    showAddIconDialog = false
                    selectedCategoryTitle = ""
                    selectedItem = null
                }
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("正在加载数据...")
    }
}

@Composable
private fun MainContent(
    categories: List<concerrox.valley.iconpackmaker.model.Category>,
    onAddIconClick: (String) -> Unit,
    onDeleteIconClick: (String, String) -> Unit,
    onEditIconClick: (String, concerrox.valley.iconpackmaker.model.IconItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(categories.size) { index ->
            val category = categories[index]
            CategoryContainer(
                category = category,
                onAddIconClick = { onAddIconClick(category.title) },
                onDeleteIconClick = { itemId -> onDeleteIconClick(category.title, itemId) },
                onEditIconClick = { item -> onEditIconClick(category.title, item) }
            )
        }
    }
}

@Composable
private fun CategoryContainer(
    category: concerrox.valley.iconpackmaker.model.Category,
    onAddIconClick: () -> Unit,
    onDeleteIconClick: (String) -> Unit,
    onEditIconClick: (concerrox.valley.iconpackmaker.model.IconItem) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = category.title,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.subtitle2,
            lineHeight = 48.sp,
            modifier = Modifier.padding(horizontal = 16.dp).height(48.dp),
        )

        IconGrid(
            items = category.items,
            categoryTitle = category.title,
            onDeleteClick = onDeleteIconClick,
            onEditClick = onEditIconClick,
            onAddItemClick = onAddIconClick
        )
    }
}

@Composable
private fun IconGrid(
    items: List<concerrox.valley.iconpackmaker.model.IconItem>,
    categoryTitle: String,
    onDeleteClick: (String) -> Unit,
    onEditClick: (concerrox.valley.iconpackmaker.model.IconItem) -> Unit,
    onAddItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 使用 FlowRow 实现真正的流式布局
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // 显示所有图标项
        items.forEach { item ->
            IconGridItem(
                item = item,
                onDeleteClick = { onDeleteClick(item.id) },
                onEditClick = { onEditClick(item) },
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            )
        }
        
        // 添加图标卡片
        AddItemCard(
            categoryTitle = categoryTitle,
            onClick = onAddItemClick,
            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
        )
    }
}