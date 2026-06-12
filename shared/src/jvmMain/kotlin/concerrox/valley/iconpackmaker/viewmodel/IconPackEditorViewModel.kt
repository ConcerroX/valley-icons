package concerrox.valley.iconpackmaker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import concerrox.valley.iconpackmaker.model.Category
import concerrox.valley.iconpackmaker.model.IconItem
import concerrox.valley.iconpackmaker.model.IconPackData
import concerrox.valley.iconpackmaker.util.AppFilterXmlParser
import concerrox.valley.iconpackmaker.util.DrawableXmlParser
import concerrox.valley.iconpackmaker.util.FilePathManager
import concerrox.valley.iconpackmaker.util.IconResourceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class IconPackEditorViewModel : ViewModel() {
    
    private val drawableXmlParser = DrawableXmlParser()
    private val appFilterXmlParser = AppFilterXmlParser()
    private val iconResourceManager = IconResourceManager()
    
    private val _uiState = MutableStateFlow(IconPackUiState())
    val uiState: StateFlow<IconPackUiState> = _uiState.asStateFlow()
    
    // 为了兼容现有代码的便捷访问属性
    val iconPackData: IconPackData
        get() = _uiState.value.iconPackData
    
    val isLoading: Boolean
        get() = _uiState.value.isLoading
    
    val errorMessage: String?
        get() = _uiState.value.errorMessage
    
    val successMessage: String?
        get() = _uiState.value.successMessage
    
    fun loadXmlFiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copyWithLoading(true).clearMessages()
            
            try {
                withContext(Dispatchers.IO) {
                    val projectRoot = FilePathManager.getProjectRoot()
                    val drawableXmlFile = FilePathManager.getDrawableXmlFile(projectRoot)
                    val appFilterXmlFile = FilePathManager.getAppFilterXmlFile(projectRoot)
                    
                    var loadedData = IconPackData()
                    
                    // 加载drawable.xml
                    if (drawableXmlFile.exists()) {
                        try {
                            loadedData = drawableXmlParser.parseDrawableXml(drawableXmlFile)
                        } catch (parseException: Exception) {
                            throw parseException
                        }
                    }
                    
                    // 加载appfilter.xml中的组件信息
                    if (appFilterXmlFile.exists()) {
                        try {
                            val componentToDrawableMap = appFilterXmlParser.parseAppFilterXml(appFilterXmlFile)

                            // 将组件信息合并到现有数据中
                            loadedData = mergeComponentInfo(loadedData, componentToDrawableMap)
                        } catch (parseException: Exception) {
                            // 不抛出异常，继续使用drawable数据
                        }
                    }
                    
                    _uiState.value = _uiState.value.copyWithIconPackData(loadedData)
                }
            } catch (e: Exception) {
                val errorMsg = "加载文件失败: ${e.message}"
                println(errorMsg)
                e.printStackTrace()
                _uiState.value = _uiState.value.copyWithError(errorMsg)
            } finally {
                _uiState.value = _uiState.value.copyWithLoading(false)
            }
        }
    }
    
    /**
     * 添加新分类
     */
    fun addCategory(title: String) {
        if (title.isNotBlank() && !iconPackData.categories.any { it.title == title }) {
            val newCategory = Category(title.trim())
            val updatedData = iconPackData.addCategory(newCategory)
            _uiState.value = _uiState.value.copyWithIconPackData(updatedData)
            
            // 立即保存到XML文件
            saveCategoryToXml(updatedData)
            
            _uiState.value = _uiState.value.copyWithSuccess("分类 '$title' 已添加并自动保存")
        } else {
            _uiState.value = _uiState.value.copyWithError("分类名称不能为空或已存在")
        }
    }
    
    /**
     * 立即保存分类到XML文件
     */
    private fun saveCategoryToXml(iconPackData: IconPackData) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val projectRoot = FilePathManager.getProjectRoot()
                    val drawableXmlFile = FilePathManager.getDrawableXmlFile(projectRoot)
                    
                    // 更新drawable.xml
                    drawableXmlParser.writeDrawableXml(iconPackData, drawableXmlFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun removeCategory(category: Category) {
        val updatedData = iconPackData.removeCategory(category)
        _uiState.value = _uiState.value
            .copyWithIconPackData(updatedData)
            .copyWithSuccess("分类 '${category.title}' 已删除")
    }
    
    /**
     * 添加带图片的图标项
     */
    fun addIconItemWithImages(
        categoryTitle: String,
        drawableName: String,
        componentName: String = "",
        foregroundImage: File?,
        backgroundImage: File?
    ) {
        viewModelScope.launch {
            if (drawableName.isBlank()) {
                _uiState.value = _uiState.value.copyWithError("图标名称不能为空")
                return@launch
            }
            
            // 验证图片文件
            foregroundImage?.let { file ->
                iconResourceManager.validateImageFile(file)
                    .onFailure { 
                        _uiState.value = _uiState.value.copyWithError("前景图片无效: ${it.message}")
                        return@launch
                    }
            }
            
            backgroundImage?.let { file ->
                iconResourceManager.validateImageFile(file)
                    .onFailure { 
                        _uiState.value = _uiState.value.copyWithError("背景图片无效: ${it.message}")
                        return@launch
                    }
            }
            
            // 检查drawable名称是否已存在
            if (iconResourceManager.isDrawableNameExists(drawableName)) {
                _uiState.value = _uiState.value.copyWithError("图标名称 '$drawableName' 已存在")
                return@launch
            }
            
            try {
                // 创建图标资源
                iconResourceManager.createAdaptiveIcon(drawableName, foregroundImage, backgroundImage)
                    .onSuccess { message ->
                        // 添加到数据模型
                        val category = iconPackData.getCategoryByTitle(categoryTitle)
                        if (category != null) {
                            val newItem = IconItem(
                                id = "${categoryTitle}_${drawableName}_${System.currentTimeMillis()}",
                                drawable = drawableName.trim(),
                                component = componentName.trim()
                            )
                            val updatedCategory = category.addItem(newItem)
                            val updatedData = iconPackData.updateCategory(updatedCategory)
                            _uiState.value = _uiState.value.copyWithIconPackData(updatedData)
                            
                            // 立即保存到XML文件
                            saveIconToXml(updatedData, newItem)
                            
                            _uiState.value = _uiState.value.copyWithSuccess("$message 并已添加到 '$categoryTitle'，已自动保存")
                        } else {
                            _uiState.value = _uiState.value.copyWithError("找不到分类: $categoryTitle")
                        }
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copyWithError("创建图标失败: ${exception.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("操作失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * 删除图标项
     */
    fun removeIconItem(categoryTitle: String, itemId: String) {
        val category = iconPackData.getCategoryByTitle(categoryTitle)
        if (category != null) {
            val updatedCategory = category.removeItemById(itemId)
            val updatedData = iconPackData.updateCategory(updatedCategory)
            _uiState.value = _uiState.value.copyWithIconPackData(updatedData)
            
            // 立即保存到XML文件
            saveChangesToXml(updatedData)
            
            _uiState.value = _uiState.value.copyWithSuccess("图标已删除并自动保存")
        }
    }
    
    /**
     * 编辑图标项
     */
    fun editIconItem(
        categoryTitle: String,
        itemId: String,
        componentName: String = "",
        foregroundImage: File?,
        backgroundImage: File?
    ) {
        viewModelScope.launch {
            // 验证图片文件
            foregroundImage?.let { file ->
                iconResourceManager.validateImageFile(file)
                    .onFailure { 
                        _uiState.value = _uiState.value.copyWithError("前景图片无效: ${it.message}")
                        return@launch
                    }
            }
            
            backgroundImage?.let { file ->
                iconResourceManager.validateImageFile(file)
                    .onFailure { 
                        _uiState.value = _uiState.value.copyWithError("背景图片无效: ${it.message}")
                        return@launch
                    }
            }
            
            try {
                val category = iconPackData.getCategoryByTitle(categoryTitle)
                if (category != null) {
                    val existingItem = category.getItemById(itemId)
                    if (existingItem != null) {
                        // 更新图标资源（如果提供了新图片）
                        if (foregroundImage != null || backgroundImage != null) {
                            iconResourceManager.updateAdaptiveIcon(
                                existingItem.drawable, 
                                foregroundImage, 
                                backgroundImage
                            ).onFailure { exception ->
                                _uiState.value = _uiState.value.copyWithError("更新图标失败: ${exception.message}")
                                return@launch
                            }
                        }
                        
                        // 更新数据模型
                        val updatedItem = existingItem.copy(component = componentName.trim())
                        val updatedCategory = category.updateItem(updatedItem)
                        val updatedData = iconPackData.updateCategory(updatedCategory)
                        _uiState.value = _uiState.value.copyWithIconPackData(updatedData)
                        
                        // 立即保存到XML文件
                        saveChangesToXml(updatedData)
                        
                        _uiState.value = _uiState.value.copyWithSuccess("图标已更新并自动保存")
                    } else {
                        _uiState.value = _uiState.value.copyWithError("找不到要编辑的图标")
                    }
                } else {
                    _uiState.value = _uiState.value.copyWithError("找不到分类: $categoryTitle")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("编辑图标失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 立即保存所有更改到XML文件
     */
    private fun saveChangesToXml(iconPackData: IconPackData) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val projectRoot = FilePathManager.getProjectRoot()
                    val drawableXmlFile = FilePathManager.getDrawableXmlFile(projectRoot)
                    val appFilterXmlFile = FilePathManager.getAppFilterXmlFile(projectRoot)
                    
                    // 更新drawable.xml
                    drawableXmlParser.writeDrawableXml(iconPackData, drawableXmlFile)
                    
                    // 更新appfilter.xml（如果存在）
                    if (appFilterXmlFile.exists()) {
                        val existingMappings = appFilterXmlParser.parseAppFilterXml(appFilterXmlFile)
                        val allItems = iconPackData.categories.flatMap { it.items }
                        appFilterXmlParser.updateAppFilterXml(appFilterXmlFile, allItems, existingMappings)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 立即保存单个图标到XML文件
     */
    private fun saveIconToXml(iconPackData: IconPackData, newItem: IconItem) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val projectRoot = FilePathManager.getProjectRoot()
                    val drawableXmlFile = FilePathManager.getDrawableXmlFile(projectRoot)
                    val appFilterXmlFile = FilePathManager.getAppFilterXmlFile(projectRoot)
                    
                    // 更新drawable.xml
                    drawableXmlParser.writeDrawableXml(iconPackData, drawableXmlFile)
                    
                    // 更新appfilter.xml（如果存在）
                    if (appFilterXmlFile.exists()) {
                        val existingMappings = appFilterXmlParser.parseAppFilterXml(appFilterXmlFile)
                        val allItems = iconPackData.categories.flatMap { it.items }
                        appFilterXmlParser.updateAppFilterXml(appFilterXmlFile, allItems, existingMappings)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    

    
    /**
     * 清除消息提示
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.clearMessages()
    }
    
    /**
     * 将appfilter.xml中的组件信息合并到IconPackData中
     */
    private fun mergeComponentInfo(
        data: IconPackData, 
        componentToDrawableMap: Map<String, String>
    ): IconPackData {
        // 创建drawable到component的反向映射
        val drawableToComponentMap = componentToDrawableMap.entries
            .groupBy({ it.value }) { it.key }
            .mapValues { entry -> entry.value.firstOrNull() ?: "" }
        

        // 更新每个分类中的图标项
        val updatedCategories = data.categories.map { category ->
            val updatedItems = category.items.map { item ->
                val component = drawableToComponentMap[item.drawable] ?: ""
                item.copy(component = component)
            }
            category.copy(items = updatedItems)
        }
        
        return data.copy(categories = updatedCategories)
    }
    
    fun checkDrawableExists(drawableName: String): Boolean {
        val projectRoot = FilePathManager.getProjectRoot()
        val drawablePaths = listOf(
            "app/src/main/res/drawable-v26",
            "app/src/main/res/drawable"
        )
        return drawablePaths.any { path ->
            val drawableFile = File("$projectRoot/$path/${drawableName}.xml")
            drawableFile.exists()
        }
    }
}