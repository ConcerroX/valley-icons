package concerrox.valley.iconpackmaker.util

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class IconResourceManager {
    
    companion object {
        private const val DRAWABLE_V26_PATH = "app/src/main/res/drawable-v26"
        private const val DRAWABLE_PATH = "app/src/main/res/drawable"
    }
    
    /**
     * 创建自适应图标资源
     * @param drawableName 完整的drawable名称（包含前缀）
     * @param foregroundImage 前景图片文件
     * @param backgroundImage 背景图片文件
     * @return 创建结果信息
     */
    fun createAdaptiveIcon(
        drawableName: String,
        foregroundImage: File?,
        backgroundImage: File?
    ): Result<String> {
        return try {
            val projectRoot = FilePathManager.getProjectRoot()
            val drawableV26Dir = File("$projectRoot/$DRAWABLE_V26_PATH")
            val drawableDir = File("$projectRoot/$DRAWABLE_PATH")
            
            // 确保目录存在
            drawableV26Dir.mkdirs()
            drawableDir.mkdirs()
            
            val createdFiles = mutableListOf<String>()
            val movedFiles = mutableListOf<String>()
            
            // 移动前景图片
            if (foregroundImage != null && foregroundImage.exists()) {
                val foregroundDest = File(drawableV26Dir, "${drawableName}_foreground.png")
                // 先复制再删除源文件，实现移动效果
                Files.copy(
                    foregroundImage.toPath(),
                    foregroundDest.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                // 删除源文件
                foregroundImage.delete()
                createdFiles.add(foregroundDest.name)
                movedFiles.add(foregroundImage.name)
            }
            
            // 移动背景图片
            if (backgroundImage != null && backgroundImage.exists()) {
                val backgroundDest = File(drawableV26Dir, "${drawableName}_background.png")
                // 先复制再删除源文件，实现移动效果
                Files.copy(
                    backgroundImage.toPath(),
                    backgroundDest.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                // 删除源文件
                backgroundImage.delete()
                createdFiles.add(backgroundDest.name)
                movedFiles.add(backgroundImage.name)
            }
            
            // 创建XML文件
            val xmlContent = generateAdaptiveIconXml(drawableName, foregroundImage, backgroundImage)
            val xmlFile = File(drawableV26Dir, "$drawableName.xml")
            xmlFile.writeText(xmlContent)
            createdFiles.add(xmlFile.name)
            
            val moveInfo = if (movedFiles.isNotEmpty()) "，已移动文件: ${movedFiles.joinToString(", ")}" else ""
            Result.success("成功创建 ${createdFiles.joinToString(", ")}$moveInfo")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成自适应图标XML内容
     * 会检查磁盘上实际存在的文件，即使参数为null也会包含已存在的层
     */
    private fun generateAdaptiveIconXml(
        drawableName: String,
        foregroundImage: File?,
        backgroundImage: File?
    ): String {
        val projectRoot = FilePathManager.getProjectRoot()
        val drawableV26Dir = File("$projectRoot/$DRAWABLE_V26_PATH")
        
        // 检查磁盘上实际存在的文件
        val foregroundFile = File(drawableV26Dir, "${drawableName}_foreground.png")
        val backgroundFile = File(drawableV26Dir, "${drawableName}_background.png")
        
        val hasForeground = foregroundImage != null || foregroundFile.exists()
        val hasBackground = backgroundImage != null || backgroundFile.exists()
        
        return buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            appendLine("<adaptive-icon xmlns:android=\"http://schemas.android.com/apk/res/android\">")
            
            if (hasBackground) {
                appendLine("    <background android:drawable=\"@drawable/${drawableName}_background\"/>")
            }
            
            if (hasForeground) {
                appendLine("    <foreground android:drawable=\"@drawable/${drawableName}_foreground\"/>")
            }
            
            appendLine("</adaptive-icon>")
        }
    }
    
    /**
     * 验证图片文件
     */
    fun validateImageFile(file: File): Result<Unit> {
        return try {
            if (!file.exists()) {
                return Result.failure(Exception("文件不存在"))
            }
            
            if (!file.isFile) {
                return Result.failure(Exception("请选择有效的图片文件"))
            }
            
            val extension = file.extension.lowercase()
            if (extension !in listOf("png", "jpg", "jpeg", "webp")) {
                return Result.failure(Exception("只支持 PNG, JPG, JPEG, WEBP 格式的图片"))
            }
            
            // 检查文件大小（限制为50MB）
            if (file.length() > 50 * 1024 * 1024) {
                return Result.failure(Exception("图片文件过大（超过50MB）"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取drawable-v26目录中的所有图标名称
     */
    fun getExistingIconNames(): List<String> {
        return try {
            val projectRoot = FilePathManager.getProjectRoot()
            val drawableV26Dir = File("$projectRoot/$DRAWABLE_V26_PATH")
            
            if (!drawableV26Dir.exists()) return emptyList()
            
            drawableV26Dir.listFiles()
                ?.filter { it.isFile && it.extension == "xml" }
                ?.map { it.nameWithoutExtension }
                ?.sorted()
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 检查drawable名称是否已存在
     */
    fun isDrawableNameExists(drawableName: String): Boolean {
        val existingNames = getExistingIconNames()
        return drawableName in existingNames
    }
    
    /**
     * 更新自适应图标资源
     * @param drawableName 完整的drawable名称（包含前缀）
     * @param foregroundImage 新的前景图片文件（可选）
     * @param backgroundImage 新的背景图片文件（可选）
     * @return 更新结果信息
     */
    fun updateAdaptiveIcon(
        drawableName: String,
        foregroundImage: File?,
        backgroundImage: File?
    ): Result<String> {
        return try {
            val projectRoot = FilePathManager.getProjectRoot()
            val drawableV26Dir = File("$projectRoot/$DRAWABLE_V26_PATH")
            
            if (!drawableV26Dir.exists()) {
                return Result.failure(Exception("资源目录不存在"))
            }
            
            val updatedFiles = mutableListOf<String>()
            val movedFiles = mutableListOf<String>()
            
            // 更新前景图片
            if (foregroundImage != null && foregroundImage.exists()) {
                val foregroundDest = File(drawableV26Dir, "${drawableName}_foreground.png")
                // 先复制再删除源文件，实现移动效果
                Files.copy(
                    foregroundImage.toPath(),
                    foregroundDest.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                // 删除源文件
                foregroundImage.delete()
                updatedFiles.add(foregroundDest.name)
                movedFiles.add(foregroundImage.name)
            }
            
            // 更新背景图片
            if (backgroundImage != null && backgroundImage.exists()) {
                val backgroundDest = File(drawableV26Dir, "${drawableName}_background.png")
                // 先复制再删除源文件，实现移动效果
                Files.copy(
                    backgroundImage.toPath(),
                    backgroundDest.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                // 删除源文件
                backgroundImage.delete()
                updatedFiles.add(backgroundDest.name)
                movedFiles.add(backgroundImage.name)
            }
            
            // 更新XML文件（如果图片有变化）
            if (foregroundImage != null || backgroundImage != null) {
                val xmlContent = generateAdaptiveIconXml(drawableName, foregroundImage, backgroundImage)
                val xmlFile = File(drawableV26Dir, "$drawableName.xml")
                xmlFile.writeText(xmlContent)
                updatedFiles.add(xmlFile.name)
            }
            
            val moveInfo = if (movedFiles.isNotEmpty()) "，已移动文件: ${movedFiles.joinToString(", ")}" else ""
            Result.success("成功更新 ${updatedFiles.joinToString(", ")}$moveInfo")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}