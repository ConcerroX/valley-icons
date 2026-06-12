package concerrox.valley.di

import concerrox.valley.data.model.xml.DrawableXml
import concerrox.valley.data.repository.IconRepository
import concerrox.valley.data.source.XmlDataSource
import concerrox.valley.data.source.XmlProvider
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.NonRecoveryUnknownChildHandler
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module

@OptIn(ExperimentalXmlUtilApi::class)
val dataModule = module {

    single(named("drawableXml")) {
        XML.v1(SerializersModule {
            polymorphic(DrawableXml.Entry::class) {
                subclass(DrawableXml.Entry.Category::class)
                subclass(DrawableXml.Entry.Item::class)
            }
        }) {
            policy {
                ignoreUnknownChildren()
            }
        }
    }

    single(named("appFilterXml")) {
        XML.v1 {
            policy {
                ignoreUnknownChildren()
            }
        }
    }

    single {
        XmlDataSource(
            provider = get(),
            drawableXml = get(named("drawableXml")),
            appFilterXml = get(named("appFilterXml"))
        )
    }

    single {
        IconRepository(xmlDataSource = get(), canEdit = !get<XmlProvider>().isReadOnly)
    }

}