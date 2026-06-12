package concerrox.valley.iconpackmaker.util

import concerrox.valley.iconpackmaker.model.IconItem
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * 处理appfilter.xml文件的工具类
 */
class AppFilterXmlParser {
    
    /**
     * 从appfilter.xml文件解析组件映射关系
     */
    fun parseAppFilterXml(file: File): Map<String, String> {
        val componentToDrawableMap = mutableMapOf<String, String>()
        
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(file)
        document.documentElement.normalize()
        
        val nodeList = document.documentElement.childNodes
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                if (element.tagName == "item") {
                    val component = element.getAttribute("component")
                    val drawable = element.getAttribute("drawable")
                    if (component.isNotEmpty() && drawable.isNotEmpty()) {
                        componentToDrawableMap[component] = drawable
                    }
                }
            }
        }
        
        return componentToDrawableMap
    }
    
    /**
     * 更新appfilter.xml文件中的组件映射关系
     * 会智能合并现有数据，避免重复条目
     */
    fun updateAppFilterXml(
        file: File, 
        newItems: List<IconItem>,
        existingMappings: Map<String, String> = emptyMap()
    ) {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.newDocument()
        
        // 创建根元素
        val rootElement = document.createElement("resources")
        document.appendChild(rootElement)
        
        // 添加默认配置
        val iconbackElement = document.createElement("iconback")
        iconbackElement.setAttribute("img1", "iconback")
        rootElement.appendChild(iconbackElement)
        
        val iconmaskElement = document.createElement("iconmask")
        iconmaskElement.setAttribute("img1", "iconmask")
        rootElement.appendChild(iconmaskElement)
        
        val scaleElement = document.createElement("scale")
        scaleElement.setAttribute("factor", "0.64")
        rootElement.appendChild(scaleElement)
        
        // 添加注释
        val comment = document.createComment(" Icons ")
        rootElement.appendChild(comment)
        
        // 构建完整的映射关系：优先使用新数据覆盖旧数据
        val mergedMappings = mutableMapOf<String, String>()
        
        // 先添加现有的映射关系
        mergedMappings.putAll(existingMappings)
        
        // 然后用新数据覆盖/添加映射关系
        newItems.filter { it.component.isNotEmpty() }.forEach { item ->
            mergedMappings[item.component] = item.drawable
        }
        
        // 写入合并后的映射关系（去重且无重复）
        mergedMappings.forEach { (component, drawable) ->
            val itemElement = document.createElement("item")
            itemElement.setAttribute("component", component)
            itemElement.setAttribute("drawable", drawable)
            rootElement.appendChild(itemElement)
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