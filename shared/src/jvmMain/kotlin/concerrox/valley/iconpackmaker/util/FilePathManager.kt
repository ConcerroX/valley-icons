package concerrox.valley.iconpackmaker.util

import java.io.File

/**
 * 文件路径管理器
 */
object FilePathManager {
    private const val RES_XML_PATH = "app/src/main/res/xml"
    private const val DRAWABLE_XML = "drawable.xml"
    private const val APPFILTER_XML = "appfilter.xml"
    
    fun getDrawableXmlFile(projectRoot: String): File {
        return File("$projectRoot/$RES_XML_PATH/$DRAWABLE_XML")
    }
    
    fun getAppFilterXmlFile(projectRoot: String): File {
        return File("$projectRoot/$RES_XML_PATH/$APPFILTER_XML")
    }
    
    fun getProjectRoot(): String {
        // 获取项目根目录 - 使用相对路径
        val currentDir = System.getProperty("user.dir")
        
        // 检查常见的项目结构
        val possiblePaths = listOf(
            currentDir,  // 直接运行的情况
            "$currentDir/..",  // 在子目录运行的情况
            "$currentDir/../.."  // 在更深的子目录运行的情况
        )
        
        for (path in possiblePaths) {
            val normalizedPath = File(path).canonicalPath
            val testFile = File("$normalizedPath/app/src/main/res/xml/drawable.xml")
            if (testFile.exists()) {
                return normalizedPath
            }
        }

        return currentDir
    }
}