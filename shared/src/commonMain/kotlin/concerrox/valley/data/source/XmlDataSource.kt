package concerrox.valley.data.source

import concerrox.valley.data.model.xml.AppFilterXml
import concerrox.valley.data.model.xml.DrawableXml
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML

class XmlDataSource(
    private val provider: XmlProvider, private val drawableXml: XML, private val appFilterXml: XML
) {

    suspend fun readDrawableXml() =
        drawableXml.decodeFromString<DrawableXml>(provider.loadXml("drawable.xml"))

    suspend fun readAppFilterXml() =
        appFilterXml.decodeFromString<AppFilterXml>(provider.loadXml("appfilter.xml"))

}