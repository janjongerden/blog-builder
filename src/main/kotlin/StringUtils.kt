fun countSubstringInString(string: String, substring: String): Int {
    if (string.isEmpty()) {
        return 0
    }
    return string.split(substring).size - 1
}
