import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ReplacementUtilsTest {

    @Test
    fun `replaceTripleBackticksWithCodeTags creates code block`() {
        val input =
            """
```
println("hello world!")
```
            """
        val expectedOutput =
            """
<pre><code>println("hello world!")
</code></pre>
            """
        val output = ReplacementUtils.replaceTripleBackticksWithCodeTags(input)

        assertEquals(expectedOutput, output)
    }

    @Test
    fun `replaceInlineBackticksWithCodeTags adds inline code tags`() {
        val expectedOutput = "I like to write <code>code</code>."

        val input = "I like to write `code`."
        val output = ReplacementUtils.replaceInlineBackticksWithCodeTags(input)

        assertEquals(expectedOutput, output)
    }
}
