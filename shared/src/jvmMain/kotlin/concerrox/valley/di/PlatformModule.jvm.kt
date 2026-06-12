package concerrox.valley.di

import concerrox.valley.data.source.IconDrawableProvider
import concerrox.valley.data.source.ProjectIconDrawableProvider
import concerrox.valley.data.source.ProjectXmlProvider
import concerrox.valley.data.source.XmlProvider
import org.koin.dsl.module
import kotlin.io.path.Path

private val ProjectResPath = Path("D:/Projects/valley-icon-pack/app/src/main/res")

actual val platformModule = module {

    single<XmlProvider> {
        ProjectXmlProvider(ProjectResPath)
    }

    single<IconDrawableProvider> {
        ProjectIconDrawableProvider(ProjectResPath)
    }

}