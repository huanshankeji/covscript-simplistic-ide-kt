package shreckye.covscript.simplisticide

import org.apache.commons.text.StringEscapeUtils

enum class LineSeparator {
    LF, CRLF, CR;

    fun toLineSeparatorString() = when (this) {
        LF -> "\n"
        CRLF -> "\r\n"
        CR -> "\r"
    }

    companion object : StringBiSerializer<LineSeparator> {
        override fun dataToString(data: LineSeparator) =
            data.name

        override fun stringToData(string: String) =
            valueOf(string)
    }
}

fun LineSeparator.serializeToString() =
    LineSeparator.dataToString(this)

fun String.deserializeToLineSeparator() =
    LineSeparator.stringToData(this)

val systemLineSeparatorString: String get() = System.lineSeparator()

fun fromLineSeparatorString(string: String) =
    when (string) {
        "\n" -> LineSeparator.LF
        "\r\n" -> LineSeparator.CRLF
        "\r" -> LineSeparator.CR
        else -> throw IllegalArgumentException(
            "Illegal line separator: ${
                StringEscapeUtils.escapeJava(
                    string
                )
            }"
        )
    }

val systemLineSeparator
    get() = fromLineSeparatorString(
        systemLineSeparatorString
    )

// Regex matches the longest in the alternation
val lineSeparatorsRegex = Regex("(\r\n)|(\n)|(\r)")

fun extractDistinctSeparators(string: String) =
    lineSeparatorsRegex.findAll(string).map { it.value }
        .distinct().map(::fromLineSeparatorString).toList()