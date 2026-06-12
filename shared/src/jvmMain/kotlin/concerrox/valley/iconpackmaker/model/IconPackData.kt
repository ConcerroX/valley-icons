package concerrox.valley.iconpackmaker.model

data class IconPackData(
    val categories: List<Category> = Category.createDefaultCategories(),
    val version: Int = 1
) {
    fun addCategory(category: Category): IconPackData {
        return copy(categories = categories + category)
    }
    
    fun removeCategory(category: Category): IconPackData {
        return copy(categories = categories - category)
    }
    
    fun getCategoryByTitle(title: String): Category? {
        return categories.find { it.title == title }
    }
    
    fun updateCategory(updatedCategory: Category): IconPackData {
        val updatedCategories = categories.map { 
            if (it.title == updatedCategory.title) updatedCategory else it 
        }
        return copy(categories = updatedCategories)
    }
    
    fun getAllDrawables(): List<String> {
        return categories.flatMap { category -> 
            category.items.map { it.drawable } 
        }.distinct()
    }
}