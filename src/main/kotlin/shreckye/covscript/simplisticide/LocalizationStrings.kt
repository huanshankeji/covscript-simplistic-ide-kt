package shreckye.covscript.simplisticide

fun IndentationType.toEnglishString() =
    when (this) {
        Indentation.Spaces::class -> "spaces"
        Indentation.Tab::class -> "tab"
        else -> throw IllegalArgumentException()
    }

fun Indentation.toEnglishString() =
    when (this) {
        is Indentation.Spaces -> "$number spaces"
        is Indentation.Tab -> "tab"
    }