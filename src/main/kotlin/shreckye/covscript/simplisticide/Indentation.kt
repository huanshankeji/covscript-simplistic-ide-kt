package shreckye.covscript.simplisticide

import kotlin.reflect.KClass

sealed class Indentation {
    abstract val text: String

    class Spaces(val number: Int) : Indentation() {
        override val text: String get() = " ".repeat(number)
    }

    object Tab : Indentation() {
        override val text: String get() = "\t"
    }

    companion object : StringBiSerializer<Indentation> {
        override fun dataToString(data: Indentation): String =
            when (data) {
                is Spaces -> "spaces(${data.number})"
                is Tab -> "tab"
            }

        override fun stringToData(string: String): Indentation =
            when {
                string.startsWith("spaces(") && string.endsWith(
                    ")"
                ) ->
                    Spaces(
                        string.substring(
                            7,
                            string.length - 1
                        ).toInt()
                    )
                string.startsWith("tab") -> Tab
                else -> throw IllegalArgumentException(
                    string
                )
            }
    }
}

typealias IndentationType = KClass<out Indentation>

fun Indentation.serializeToString() =
    Indentation.dataToString(this)

fun String.deserializeToIndentation() =
    Indentation.stringToData(this)