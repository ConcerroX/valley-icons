package concerrox.valley.data.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ProjectXmlProvider(private val projectPath: Path) : XmlProvider {

    override val isReadOnly = false

    override suspend fun loadXml(filename: String) = withContext(Dispatchers.IO) {
        (projectPath / "xml" / filename).readText()
    }

    override suspend fun saveXml(filename: String, content: String) = withContext(Dispatchers.IO) {
        (projectPath / "xml" / filename).writeText(content)
    }

}