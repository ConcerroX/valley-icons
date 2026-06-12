package concerrox.valley.data.model.xml

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlIgnoreWhitespace
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("resources")
data class AppFilterXml(
    @XmlIgnoreWhitespace @XmlValue val items: List<@Polymorphic Item>
) {

    @Serializable
    @XmlSerialName("item")
    data class Item(
        val component: String, val drawable: String
    )

}