package concerrox.valley.iconpackmaker.viewmodel

import concerrox.valley.iconpackmaker.model.IconPackData

/**
 * 图标包编辑器的UI状态数据类
 */
data class IconPackUiState(
    val iconPackData: IconPackData = IconPackData(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    // 便捷的copy方法用于更新特定字段
    fun copyWithIconPackData(data: IconPackData) = copy(iconPackData = data)
    fun copyWithLoading(loading: Boolean) = copy(isLoading = loading)
    fun copyWithError(message: String?) = copy(errorMessage = message)
    fun copyWithSuccess(message: String?) = copy(successMessage = message)
    fun clearMessages() = copy(errorMessage = null, successMessage = null)
}