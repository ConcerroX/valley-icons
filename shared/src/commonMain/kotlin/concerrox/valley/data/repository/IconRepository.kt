package concerrox.valley.data.repository

import concerrox.valley.data.model.Icon
import concerrox.valley.data.model.IconCategory
import concerrox.valley.data.model.xml.DrawableXml
import concerrox.valley.data.source.XmlDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IconRepository(private val xmlDataSource: XmlDataSource, val canEdit: Boolean) {

    suspend fun getCategories() = withContext(Dispatchers.IO) {
        val drawableXml = xmlDataSource.readDrawableXml()
        val appFilterXml = xmlDataSource.readAppFilterXml()

        val mutableCategories = mutableListOf<IconCategory.Mutable>()
        val adaptationMap = appFilterXml.items.groupBy(
            keySelector = { it.drawable },
            valueTransform = { it.component })

        drawableXml.entries.fold(mutableCategories) { acc, entry ->
            when (entry) {
                is DrawableXml.Entry.Category -> {
                    acc.add(IconCategory.Mutable(entry.title))
                }

                is DrawableXml.Entry.Item -> {
                    val componentInfos = adaptationMap[entry.drawable] ?: emptyList()
                    val icon = Icon(entry.drawable, componentInfos)
                    acc.last().items.add(icon)
                }
            }
            return@fold acc
        }

        mutableCategories.map { IconCategory(it.title, it.items.toList()) }
    }

}