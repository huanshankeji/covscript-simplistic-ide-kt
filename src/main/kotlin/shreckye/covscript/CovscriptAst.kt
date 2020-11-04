package shreckye.covscript

import shreckye.covscript.simplisticide.kotlin.FList
import shreckye.covscript.simplisticide.kotlin.FNil
import shreckye.covscript.simplisticide.kotlin.cons

typealias CovscriptAstNode = List<Pair<String, Value>>

sealed class Value
class StringValue(string: String) : Value()
class NodeValue(node: CovscriptAstNode) : Value()

val astExpressionRegex = Regex("< Expression: (.*) >")
fun parseCovscriptAstDump(text: String): CovscriptAstNode {
    val expressionMatchResult = text.lineSequence().mapNotNull { astExpressionRegex.matchEntire(text) }.first()
    val astText = expressionMatchResult.groupValues[0]

    return parseCovscriptAst(astText)
}

class ParseException(message: String) : Exception(message)

fun parseCovscriptAst(text: String): CovscriptAstNode =
    astNode(text, 0).run {
        if (second != text.length) throw ParseException("not completely parsed")
        first
    }


/* Node -> "<" Spaces NodeContent Spaces ">"
* NodeContent -> Name Spaces "=" Spaces (StringValue | Node)
* Name -> r"[w ]*"
* StringValue -> r"\"?+\"" | Node
* Spaces -> r" *" */
@Suppress("NAME_SHADOWING")
fun astNode(text: String, index: Int): Pair<CovscriptAstNode, Int> {
    val i1 = char(text, index, '<').second
    val i2 = spaces(text, i1).second
    val (nameValues, i3) = zeroOrMultipleWithGaps(text, i2, { text, index -> nameValue(text, index) }, ::spaces)
    val i4 = spaces(text, i3).second

    return nameValues to i4
}


private fun char(text: String, index: Int, char: Char): Pair<Char, Int> {
    val sChar = text[index]
    if (sChar != char) throw ParseException("expected $char")
    return sChar to index + 1
}

private fun <T> zeroOrMultipleFList(
    text: String, index: Int,
    each: (text: String, index: Int) -> Pair<T, Int>
): Pair<FList<T>, Int> =
    try {
        val (headNode, i1) = each(text, index)
        val (tailNodes, i2) = zeroOrMultipleFList(text, i1, each)
        (headNode cons tailNodes) to i2
    } catch (e: ParseException) {
        FNil to index
    }

private fun <T> zeroOrMultiple(
    text: String, index: Int,
    each: (text: String, index: Int) -> Pair<T, Int>
): Pair<List<T>, Int> =
    zeroOrMultipleFList(text, index, each).run { first.toList() to second }

private fun <T> zeroOrMultipleWithGaps(
    text: String, index: Int,
    each: (text: String, index: Int) -> Pair<T, Int>,
    gapEach: (text: String, index: Int) -> Pair<*, Int>
): Pair<List<T>, Int> = TODO()

@Suppress("NAME_SHADOWING")
private fun spaces(text: String, index: Int) =
    zeroOrMultiple(text, index) { text, index -> char(text, index, ' ') }

private fun nameValue(text: String, index: Int): Pair<Pair<String, Value>, Int> {
    TODO("AST strings are already unescaped, making parsing hard")
}