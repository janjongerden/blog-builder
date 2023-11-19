package src.main.kotlin

import Props.DESCRIPTION
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

private val blogArticles = HashSet<BlogArticle>()
private val simpleTags = setOf(TITLE, DESCRIPTION)
private lateinit var cssFileNames:Map<String, String>
private lateinit var jsFileNames:Map<String, String>

fun main() {

    createOutputDir()

    copyStaticFiles()

    readBlogFiles()

    generateBlogHtml()

    generateTagPages()
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
    blogArticles.forEach { blog ->
        val file = File(outputDir + blog.getHtmlFileName())

        file.createNewFile()

        file.appendText(enrichTemplate(head, blog))
        file.appendText(enrichTemplate(blog.getContent(), blog))
        file.appendText(enrichTemplate(bottom, blog))
    }
}

fun generateTagPages() {
    val tags = blogArticles.map { blog -> blog.getTags() }
        .flatten()
        .toSet()

    tags.forEach { tag ->
        val file = File("${outputDir}/tag_${tag}.html")

        file.createNewFile()

        val tagPage = TagPage(tag)

        val relatedBlogs = blogArticles.filter{blogArticle -> blogArticle.getTags().contains(tag) }

        val relatedBlogList = generateBlogList(relatedBlogs)

        file.appendText(enrichTemplate(head, tagPage))
        file.appendText(relatedBlogList)
        file.appendText(enrichTemplate(bottom, tagPage))
    }
}

fun enrichTemplate(content: String, blog: BlogElement): String {
    var enriched = content
    for (tag: String in simpleTags) {
        enriched = enriched.replace("\${$tag}", blog.getProperty(tag))
    }
    if (content.contains(BLOG_LISTING)) {
        val listing = generateBlogList(blogArticles)
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

fun getRelatedBlogs(blog: BlogElement): Collection<BlogArticle> {
    val tags = blog.getTags()
    if (tags.isEmpty()) {
        return emptyList()
    }
    return blogArticles.filter { b -> b != blog}
        .filter { b -> b.getTags().intersect(tags).isNotEmpty() }
        .take(3)
}

fun generateBlogList(blogArticles: Collection<BlogArticle>): String {
    var html = "<ul class=blogList>"
    blogArticles.filter { blog -> blog.isListable() }
        .sortedByDescending { blog -> blog.getDate() }
        .forEach { blog ->
            run {
                html +=
                    """
                    <li>
                        <a href="${blog.getHtmlFileName()}">${blog.getProperty(TITLE)}</a>
                        <div class=articleDescription>${blog.getProperty(DESCRIPTION)}</div>
                    </li>
                    """
            }
        }
    html += "</ul>"
    return html
}

fun addBlog(path: Path) {
    val blogArticle = BlogArticle(path)
    println("Found blog " + blogArticle.getName())
    blogArticles.add(blogArticle)
}

fun readFile(fileName: String): String {
    return File(fileName).readText(Charsets.UTF_8)
}
