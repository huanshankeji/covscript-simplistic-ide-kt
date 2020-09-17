package shreckye.covscript.simplisticide

sealed class Indentation {
    abstract fun getString(): String

    class Spaces(val n: Int) : Indentation() {
        override fun getString(): String = " ".repeat(n)
    }

    object Tab : Indentation() {
        override fun getString(): String = "\t"
    }

    companion object : StringBiSerializer<Indentation> {
        val DEFAULT = Spaces(4)

        override fun dataToString(data: Indentation): String =
            when (data) {
                is Spaces -> "spaces(${data.n})"
                is Tab -> "tab"
            }

        override fun stringToData(string: String): Indentation =
            when {
                string.startsWith("spaces(") && string.endsWith(")") ->
                    Spaces(string.substring(7, string.length - 1).toInt())
                string.startsWith("tab") -> Tab
                else -> throw IllegalArgumentException(string)
            }
    }
}

fun Indentation.serializeToString() = Indentation.dataToString(this)
fun String.deserializeToIndentation() = Indentation.stringToData(this)