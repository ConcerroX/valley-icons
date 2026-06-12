package concerrox.valley.iconpackmaker.util

import concerrox.valley.iconpackmaker.model.Category
import concerrox.valley.iconpackmaker.model.IconItem
import concerrox.valley.iconpackmaker.model.IconPackData
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class DrawableXmlParser {
    
    fun parseDrawableXml(file: File): IconPackData {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(file)
        document.documentElement.normalize()
        
        val categories = mutableListOf<Category>()
        var currentCategory: Category? = null
        var version = 1
        
        val nodeList = document.documentElement.childNodes
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                when (element.tagName) {
                    "version" -> {
                        version = element.textContent.toIntOrNull() ?: 1
                    }
                    "category" -> {
                        val title = element.getAttribute("title")
                        currentCategory = Category(title)
                        categories.add(currentCategory)
                    }
                    "item" -> {
                        val drawable = element.getAttribute("drawable")
                        if (drawable.isNotEmpty() && currentCategory != null) {
                            val iconItem = IconItem.fromDrawableName(drawable)
                            currentCategory = currentCategory.addItem(iconItem)
                            // 更新categories列表中的对应项
                            val index = categories.indexOfFirst { it.title == currentCategory.title }
                            if (index >= 0) {
                                categories[index] = currentCategory
                            }
                        }
                    }
                }
            }
        }
        
        return IconPackData(categories, version)
    }
    
    /**
     * 将图标包数据写入drawable.xml文件
     */
    fun writeDrawableXml(data: IconPackData, file: File) {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.newDocument()
        
        // 创建根元素
        val rootElement = document.createElement("resources")
        document.appendChild(rootElement)
        
        // 添加版本信息
        val versionElement = document.createElement("version")
        versionElement.textContent = data.version.toString()
        rootElement.appendChild(versionElement)
        
        // 添加分类和图标项
        data.categories.forEach { category ->
            val categoryElement = document.createElement("category")
            categoryElement.setAttribute("title", category.title)
            rootElement.appendChild(categoryElement)
            
            category.items.forEach { item ->
                val itemElement = document.createElement("item")
                itemElement.setAttribute("drawable", item.drawable)
                rootElement.appendChild(itemElement)
            }
        }
        
        // 写入文件
        writeXmlToFile(document, file)
    }
    
    private fun writeXmlToFile(document: Document, file: File) {
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        
        val source = DOMSource(document)
        val result = StreamResult(file)
        transformer.transform(source, result)
    }
}