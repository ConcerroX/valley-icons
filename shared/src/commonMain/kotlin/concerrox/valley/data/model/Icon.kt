package concerrox.valley.data.model

import java.io.Serializable
import java.util.Locale

data class Icon(val drawable: String, val adaptedComponentInfos: List<String>) : Serializable {

    val name = drawable.substringAfter('_').split("_").joinToString(" ") {
        it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else it }
    }

}

data class IconCategory(
    val title: String, val items: List<Icon>
) : Serializable {
    internal data class Mutable(
        val title: String, val items: MutableList<Icon> = mutableListOf()
    )
}

data class IconAdaptation(
    val drawable: String, val componentInfo: List<String>
)