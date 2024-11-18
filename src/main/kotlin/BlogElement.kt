abstract class BlogElement {

    abstract fun getProperty(tag: String): String

    abstract fun getTags(): Set<String>

    abstract fun getRoachCount(): Int
}
