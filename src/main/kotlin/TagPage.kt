package src.main.kotlin

class TagPage(tagName: String) : BlogElement() {

    private val props: Map<String, String>

    init {
        props = mapOf(
            "title" to "Blogs tagged '$tagName'",
        )
    }

    override fun getProperty(tag: String): String {
        return props.getOrDefault(tag, "")
    }

    override fun getTags(): Set<String> {
        return emptySet()
    }

    override fun getRoachCount(): Int {
        return 0
    }
}
