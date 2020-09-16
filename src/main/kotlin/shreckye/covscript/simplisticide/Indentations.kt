package shreckye.covscript.simplisticide

sealed class Indentation {
    abstract fun getString(): String
}

class SpacesIndentation(val numSpaces: Int) : Indentation() {
    override fun getString(): String = " ".repeat(numSpaces)
}

object TabIndentation : Indentation() {
    override fun getString(): String = "\t"
}