package concerrox.valley.data.repository//package concerrox.valley.data.repository
//
//import concerrox.valley.data.model.xml.DrawableXml
//import concerrox.valley.data.source.XmlDataSource
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.decodeFromString
//import kotlinx.serialization.modules.SerializersModule
//import kotlinx.serialization.modules.polymorphic
//import kotlinx.serialization.modules.subclass
//import nl.adaptivity.xmlutil.serialization.XML
//import kotlin.io.path.readText
//
//class ProjectIconRepository(
//    private val dataSource: XmlDataSource,
////    private val appFilterXmlPath: Path = Path.of("D:\\Projects\\valley-icon-pack\\app\\src\\main\\res\\xml\\appfilter.xml"),
////    private val drawableXmlPath: Path = Path.of("D:\\Projects\\valley-icon-pack\\app\\src\\main\\res\\xml\\drawable.xml")
//) : IconRepository() {
//
//    override suspend fun load() {
//        withContext(Dispatchers.IO) {
//            val xml = XML.v1(SerializersModule {
//                polymorphic(DrawableXml.Entry::class) {
//                    subclass(DrawableXml.Entry.Category::class)
//                    subclass(DrawableXml.Entry.Item::class)
//                }
//            })
//            val drawableXml = xml.decodeFromString<DrawableXml>(drawableXmlPath.readText())
//            println(drawableXml.entries)
//        }
//    }
//
//}