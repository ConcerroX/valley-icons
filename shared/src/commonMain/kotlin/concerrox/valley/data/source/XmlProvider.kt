package concerrox.valley.data.source

interface XmlProvider {
    val isReadOnly: Boolean
    suspend fun loadXml(filename: String): String
    suspend fun saveXml(filename: String, content: String)
}