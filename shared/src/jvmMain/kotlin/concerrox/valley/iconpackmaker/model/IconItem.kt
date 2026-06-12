package concerrox.valley.iconpackmaker.model

/**
 * 表示单个图标项的数据模型
 */
data class IconItem(
    val id: String = "", // 唯一标识符
    val drawable: String = "", // drawable资源名称
    val component: String = "" // 组件信息（用于appfilter.xml）
) {
    companion object {
        fun fromDrawableName(drawableName: String): IconItem {
            return IconItem(
                id = drawableName,
                drawable = drawableName,
                component = ""
            )
        }
    }
}