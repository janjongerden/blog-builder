import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.name

private const val outputDir: String = "/tmp/bloggin/"
private val head = readFile("templates/head.template")
private val bottom = readFile("templates/bottom.template")

private val blogs = HashSet<Blog>()
private val supportedTags = setOf("title")

fun main() {

    createOutputDir()

    copyStaticFiles()

    readBlogFiles()

    generateBlogHtml()
}

fun copyStaticFiles() {
    copyStaticDir("css/")
    copyStaticDir("img/")
}

fun copyStaticDir(dirName: String) {

    val targetDirName = outputDir + dirName

    Files.walk(Paths.get(dirName))
            .filter { file -> !file.isDirectory() }
            .forEach { file ->
                run {
                    println("copying '$file'")
                    val source = File(dirName + file.name)
                    val target = File(targetDirName + file.fileName)
                    source.copyTo(target, overwrite = true)
                }
            }
}

fun readBlogFiles() {
    val blogFiles = Paths.get("blogs/")
    Files.walk(blogFiles)
            .filter { item -> item.name.endsWith(".blog") }
            .forEach { addBlog(it) }
}

fun createOutputDir() {
    val dir = File(outputDir)
    // remove any existing files
    dir.deleteRecursively()
    // (re)create
    dir.mkdirs()
}

fun generateBlogHtml() {
    blogs.forEach { blog ->
        val file = File(outputDir + blog.getHtmlFileName())

        if (file.exists())
            file.delete()

        file.createNewFile()

        file.appendText(enrichTemplate(head, blog))
        file.appendText(blog.getContent())
        file.appendText(bottom)
    }
}

fun enrichTemplate(content: String, blog: Blog): String {
    var enriched = content
    for (tag: String in supportedTags) {
        enriched = enriched.replace("\${$tag}", blog.getProperty(tag))
    }
    return enriched
}

fun addBlog(path: Path) {
    val blog = Blog(path)
    println("Found blog " + blog.getName())
    blogs.add(blog)
}

fun readFile(fileName: String): String {
    return File(fileName).readText(Charsets.UTF_8)
}
