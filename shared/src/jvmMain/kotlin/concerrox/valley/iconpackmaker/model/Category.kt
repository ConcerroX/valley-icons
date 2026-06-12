package concerrox.valley.iconpackmaker.model

/**
 * 表示图标的分类
 */
data class Category(
    val title: String = "", // 分类标题
    val items: List<IconItem> = emptyList() // 该分类下的图标项
) {
    fun addItem(item: IconItem): Category {
        return copy(items = items + item)
    }
    
    fun removeItem(item: IconItem): Category {
        return copy(items = items - item)
    }
    
    fun removeItemById(id: String): Category {
        return copy(items = items.filter { it.id != id })
    }
    
    fun getItemById(id: String): IconItem? {
        return items.find { it.id == id }
    }
    
    fun updateItem(updatedItem: IconItem): Category {
        return copy(items = items.map { if (it.id == updatedItem.id) updatedItem else it })
    }
    
    companion object {
        const val CATEGORY_SYSTEM = "System"
        const val CATEGORY_GOOGLE = "Google"
        const val CATEGORY_OTHERS = "Others"
        
        fun createDefaultCategories(): List<Category> {
            return listOf(
                Category(CATEGORY_SYSTEM),
                Category(CATEGORY_GOOGLE),
                Category(CATEGORY_OTHERS)
            )
        }
    }
}