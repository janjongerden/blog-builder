class ReplacementUtils {

    companion object {
        fun replaceTripleBackticksWithCodeTags(text: String): String {
            val count = countSubstringInString(text, "```")
            if (count == 0) {
                return text
            } else if (count % 2 == 1) {
                throw IllegalArgumentException("The text contains an odd number of triple backticks! (${count} occurrences)")
            }
            var replacement = text
            while (replacement.contains("```")) {
                // replacing the newline prevents a leading empty line in code blocks
                // weird thing is that the `\\s` part is needed: it is not in the original input?
                replacement = replacement.replaceFirst(Regex("```\\s*\n"), "<pre><code>")
                replacement = replacement.replaceFirst("```", "</code></pre>")
            }
            return replacement
        }

        fun replaceInlineBackticksWithCodeTags(text: String): String {
            val count = text.count { it == '`' }
            if (count == 0) {
                return text
            } else if (count % 2 == 1) {
                throw IllegalArgumentException("The text contains an odd number of backticks! (${count} occurrences)")
            }
            var replacement = text
            while (replacement.contains("`")) {
                replacement = replacement.replaceFirst("`", "<code>")
                replacement = replacement.replaceFirst("`", "</code>")
            }
            return replacement
        }
    }
}