package src.main.kotlin

import Props.TITLE
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.math.abs

private const val BLOG_LISTING:String = "\${blog_listing}"
private const val RELATED_BLOGS:String = "\${related_blogs}"
private const val ARTICLE_TAGS:String = "\${article_tags}"
private const val ROACH_JS:String = "\${roach_js}"
private const val outputDir: String = "/tmp/bloggin/"
private val head = readFile("templates/head.template")
private val bottom = readFile("templates/bottom.template")

private val blogs = HashSet<Blog>()
private val simpleTags = setOf(TITLE)
private lateinit var cssFileNames:Map<String, String>
private lateinit var jsFileNames:Map<String, String>

fun main() {

    createOutputDir()

    copyStaticFiles()

    readBlogFiles()

    generateBlogHtml()
}

fun copyStaticFiles() {
    cssFileNames = copyAndHashStaticDir("css/")
    copyAndHashStaticDir("img/", false)
    jsFileNames = copyAndHashStaticDir("js/")
}

fun copyAndHashStaticDir(dirName: String, hashNames: Boolean = true): Map<String, String> {

    val targetDirName = outputDir + dirName

    val hashedNames = HashMap<String, String>()

    Files.walk(Paths.get(dirName))
            .filter { file -> !file.isDirectory() }
            .forEach { file ->
                run {
                    println("copying '$file'")
                    val source = File(dirName + file.name)
                    val targetFileName:String
                    if (hashNames) {
                        val hashedName = hash(source)
                        targetFileName = targetDirName + hashedName
                        hashedNames[dirName + file.fileName] = dirName + hashedName
                    } else {
                        targetFileName = targetDirName + source.name
                    }
                    val target = File(targetFileName)
                    source.copyTo(target, overwrite = true)
                }
            }
    return hashedNames
}

fun hash(file: File): String {
    val hash = abs(file.readBytes().contentHashCode())
    return file.nameWithoutExtension + "_" + hash + "." + file.extension
}

fun readBlogFiles() {
    val blogFiles = Paths.get("blogs/")
    Files.walk(blogFiles)
            .filter { item -> item.name.endsWith(".blog") }
            .forEach { addBlog(it) }
}

fun createOutputDir() {
    println("Using output directory '$outputDir'")

    val dir = File(outputDir)
    // remove any existing files
    dir.deleteRecursively()
    // (re)create
    dir.mkdirs()
}

fun generateBlogHtml() {
    blogs.forEach { blog ->
        val file = File(outputDir + blog.getHtmlFileName())

        file.createNewFile()

        file.appendText(enrichTemplate(head, blog))
        file.appendText(enrichTemplate(blog.getContent(), blog))
        file.appendText(enrichTemplate(bottom, blog))
    }
}

fun enrichTemplate(content: String, blog: Blog): String {
    var enriched = content
    for (tag: String in simpleTags) {
        enriched = enriched.replace("\${$tag}", blog.getProperty(tag))
    }
    if (content.contains(BLOG_LISTING)) {
        val listing = generateBlogList(blogs)
        enriched = enriched.replace(BLOG_LISTING, listing)
    }
    if (content.contains(ARTICLE_TAGS)) {
        val tags = generateTags(blog.getTags())
        enriched = enriched.replace(ARTICLE_TAGS, tags)
    }
    if (content.contains(RELATED_BLOGS)) {
        val relatedBlogs = getRelatedBlogs(blog)
        var relatedBlogList = ""
        if (relatedBlogs.isNotEmpty()) {
            relatedBlogList = "<span class=related>related blogs:</span><br/>" + generateBlogList(relatedBlogs)
        }
        enriched = enriched.replace(RELATED_BLOGS, relatedBlogList)
    }
    if (content.contains(ROACH_JS)) {
        val roachCount = blog.getRoachCount()
        var roachJs = ""
        if (roachCount > 0) {
            roachJs = """
                <script src="js/roaches.js"></script>
                <script>
                    window.onload = () => {
                        roachesAreGo($roachCount);
                    };
                </script>
                    """
        }
        enriched = enriched.replace(ROACH_JS, roachJs)
    }
    for (entry in cssFileNames) {
        enriched = enriched.replace(entry.key, entry.value)
    }
    for (entry in jsFileNames) {
        enriched = enriched.replace(entry.key, entry.value)
    }
    return enriched
}

fun getRelatedBlogs(blog: Blog): Collection<Blog> {
    val tags = blog.getTags()
    if (tags.isEmpty()) {
        return emptyList()
    }
    return blogs.filter { b -> b != blog}
        .filter { b -> b.getTags().intersect(tags).isNotEmpty() }
        .take(3)
}

fun generateBlogList(blogs: Collection<Blog>): String {
    var html = "<ul>"
    blogs.filter { blog -> blog.isListable() }
        .sortedByDescending { blog -> blog.getDate() }
        .forEach { blog ->
            run {
                html +=
                    """
                    <li>
                        <a href="${blog.getHtmlFileName()}">${blog.getProperty(TITLE)}</a>
                    </li>
                    """
            }
        }
    html += "</ul>"
    return html
}

fun generateTags(tags: Collection<String>): String {
    if (tags.isEmpty()) {
        return ""
    }
    var html = "<div class=tags>"
    tags.forEach { tag ->
            run {
                html +=
                    """
                    <span class=tagItem>
                        $tag
                    </span>
                    """
            }
        }
    html += "</div> <br/>"
    return html
}

fun addBlog(path: Path) {
    val blog = Blog(path)
    println("Found blog " + blog.getName())
    blogs.add(blog)
}

fun readFile(fileName: String): String {
    return File(fileName).readText(Charsets.UTF_8)
}
