package shreckye.covscript.simplisticide

import org.apache.commons.text.StringEscapeUtils

enum class LineSeparator {
    LF, CRLF, CR;

    companion object: StringSerializer<LineSeparator> {
        override fun dataToString(data: LineSeparator) = when (data) {

        }
        override fun stringToData(str: String) = when (str) {
            "\n" -> LF
            "\r\n" -> CRLF
            "\r" -> LineSeparator.CR
            else -> throw IllegalArgumentException(
                "Illegal line separator: ${StringEscapeUtils.escapeJava(str)}"
            )
        }
    }
}

val SYSTEM_LINE_SEPARATOR_STRING: String = System.lineSeparator()
val SYSTEM_LINE_SEPARATOR =