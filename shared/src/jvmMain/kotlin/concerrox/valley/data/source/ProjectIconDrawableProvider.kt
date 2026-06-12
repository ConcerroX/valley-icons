package concerrox.valley.data.source

import java.nio.file.Path
import kotlin.io.path.div

class ProjectIconDrawableProvider(private val resPath: Path): IconDrawableProvider {

    override fun getDrawable(name: String): Any {
        return (resPath / "drawable-v26" / "$name.png").toFile()
    }

}