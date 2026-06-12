package concerrox.valley.data.model.xml

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlIgnoreWhitespace
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("resources")
data class DrawableXml(
    @XmlIgnoreWhitespace @XmlValue var entries: List<@Polymorphic Entry>
) {

    @Serializable
    @Polymorphic
    sealed class Entry {

        @Serializable
        @XmlSerialName("category")
        data class Category(val title: String) : Entry()

        @Serializable
        @XmlSerialName("item")
        data class Item(val drawable: String) : Entry()

    }

}