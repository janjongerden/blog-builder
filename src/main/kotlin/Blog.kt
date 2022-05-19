import java.io.File
import java.nio.file.Path
import kotlin.io.path.name

class Blog(private val path: Path) {


    private val props = HashMap<String, String>()
    private val contentHeader = "--content"
    private var blogContent = ""

    init {
        var parsingProps = true
        File("blogs/" + path.name).forEachLine { line ->
            if (parsingProps) {
                if (line == contentHeader) {
                    parsingProps = false
                } else {
                    addProperty(line)
                }
            } else {
                blogContent += "$line \n"
            }
        }
        if (parsingProps) {
            System.err.println("no content header found!")
            throw IllegalStateException("Blog without content header.")
        }
    }

    private fun addProperty(line: String) {
        if (!line.contains("=")) {
            throw IllegalStateException("Property line without '='.")
        }
        val (key, value) = line.split("=")

        if (!Props.knownProps.contains(key))
            throw IllegalArgumentException("The property '$key' is unknown.")

        props[key] = value.trim()
    }

    fun getName() : String {
        return path.name
    }

    fun getDate() : String {
        return props.getOrDefault("date", "1970-01-01")
    }

    fun getContent(): String {
        return blogContent
    }

    fun getHtmlFileName(): String {
        return path.name.replace(".blog", ".html")
    }

    fun getProperty(tag: String): String {
        return props.getOrDefault(tag, "")
    }

    fun isListable(): Boolean {
        return props.getOrDefault(Props.SHOW_IN_BLOG_LIST, "true").toBoolean()
    }
}
