package shreckye.covscript.simplisticide.tornadofx

fun String.isPositiveInt(): Boolean {
    val number = toIntOrNull()
    return number !== null && number > 0
}

fun String.isPositiveDouble(): Boolean {
    val number = toDoubleOrNull()
    return number !== null && number > 0.0
}