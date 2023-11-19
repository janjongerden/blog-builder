package src.main.kotlin

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
                    <a href="tag_${tag}.html">$tag</a>
                </span>
                """
        }
    }
    html += "</div> <br/>"
    return html
}


