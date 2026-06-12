package concerrox.valley.data.source

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidXmlProvider(private val context: Context) : XmlProvider {

    override val isReadOnly = true

    override suspend fun loadXml(filename: String) = withContext(Dispatchers.IO) {
        context.applicationContext.assets.open(filename).use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        }
    }

    override suspend fun saveXml(filename: String, content: String) = withContext(Dispatchers.IO) {
        throw UnsupportedOperationException("Cannot save XML on Android")
    }

}