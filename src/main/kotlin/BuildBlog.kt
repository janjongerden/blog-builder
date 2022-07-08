package src.main.kotlin

import Blog
import Props.TAGS
import Props.TITLE
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.name

private const val BLOG_LISTING:String = "\${blog_listing}"
private const val ARTICLE_TAGS:String = "\${article_tags}"
private const val CSS_INCLUDES:String = "\${css_includes}"
private const val outputDir: String = "/tmp/bloggin/"
private val head = readFile("templates/head.template")
private val bottom = readFile("templates/bottom.template")

private val blogs = HashSet<Blog>()
private val simpleTags = setOf(TITLE)
private lateinit var cssFileNames:Collection<String>

fun main() {

    createOutputDir()

    copyStaticFiles()

    readBlogFiles()

    generateBlogHtml()
}

fun copyStaticFiles() {
    cssFileNames = copyAndHashStaticDir("css/")
    copyAndHashStaticDir("img/", false)
}

fun copyAndHashStaticDir(dirName: String, hashNames: Boolean = true): Set<String> {

    val targetDirName = outputDir + dirName

    val hashedNames = HashSet<String>()

    Files.walk(Paths.get(dirName))
            .filter { file -> !file.isDirectory() }
            .forEach { file ->
                run {
                    println("copying '$file'")
                    val source = File(dirName + file.name)
                    val targetFileName = if (hashNames) targetDirName + hash(source) else targetDirName + source.name
                    hashedNames.add(targetFileName)
                    val target = File(targetFileName)
                    source.copyTo(target, overwrite = true)
                }
            }
    return hashedNames
}

fun hash(file: File): String {
    return file.nameWithoutExtension + "_" + file.readBytes().hashCode() + "." + file.extension
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
        val listing = generateBlogList()
        enriched = enriched.replace(BLOG_LISTING, listing)
    }
    if (content.contains(ARTICLE_TAGS)) {
        val tags = generateTags(blog.getProperty(TAGS))
        enriched = enriched.replace(ARTICLE_TAGS, tags)
    }
    if (content.contains(CSS_INCLUDES)) {
        val cssIncludes = generateCssIncludes()
        enriched = enriched.replace(CSS_INCLUDES, cssIncludes)
    }
    return enriched
}

fun generateBlogList(): String {
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

fun generateTags(tags: String?): String {
    if (tags.isNullOrBlank()) {
        return ""
    }
    var html = "<div class=tags>"
    tags.split(",")
        .map { tag -> tag.trim() }
        .forEach { tag ->
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

fun generateCssIncludes(): String {
    var html = ""
    cssFileNames
        .forEach { css ->
            run {
                html +=
                    """
                        <link rel="stylesheet" href="$css">
                    """
            }
        }
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
