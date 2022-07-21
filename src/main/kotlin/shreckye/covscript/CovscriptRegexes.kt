package shreckye.covscript

val commentRegex = Regex("#.*")
val stringLiteralRegex = Regex("\"[^\"]*\"")
val numberLiteralRegex = Regex("\\d+(\\.\\d+)?")

val preprocessingStatementRegex = Regex("@\\w*")

val keywords = listOf(
    "and",
    "or",
    "not",
    "typeid",
    "new",
    "null",
    "local",
    "global",
    "true",
    "false",
    "gcnew",

    "import",
    "as",
    "package",
    "namespace",
    "using",
    "struct",
    "class",
    "extends",
    "block",
    "var",
    "constant",
    "if",
    "else",
    "switch",
    "case",
    "default",
    "end",
    "while",
    "loop",
    "until",
    "for",
    "foreach",
    "in",
    "do",
    "break",
    "continue",
    "function",
    "override",
    "return",
    "try",
    "catch",
    "throw"
)
val keywordRegex =
    Regex("(?:^|\\W)(${keywords.joinToString("|")})(?:$|\\W)")

//val literalRegex

val symbols = listOf(
    "+",
    "-",
    "*",
    "/",
    "%",
    "^",
    ".",
    "->",
    "<",
    ">",
    "<=",
    ">=",
    "==",
    "!=",
    "&&",
    "||",
    "!",
    "++",
    "--",
    "=",
    "+=",
    "-=",
    "*=",
    "/=",
    "%=",
    "^="
)
val symbolRegex = Regex(
    symbols.asSequence().map(Regex::escape)
        .joinToString("|")
)
val functionRegex = Regex("(\\w+) *\\((.*)\\)")
val variableRegex = Regex("\\w+")

typealias SyntaxSegmentRanges = List<IntRange>

class CovscriptSyntaxSegmentss(
    val comments: SyntaxSegmentRanges,
    val stringLiterals: SyntaxSegmentRanges,
    val numberLiterals: SyntaxSegmentRanges,
    val preprocessingStatements: SyntaxSegmentRanges,
    val keywords: SyntaxSegmentRanges,
    val symbols: SyntaxSegmentRanges,
    val functions: SyntaxSegmentRanges,
    val variables: SyntaxSegmentRanges
)

/*private fun Regex.findAllSegmentRangeList(input: CharSequence) =
    findAll(input).map { it.range }.toList()*/
private fun Regex.findAllSegmentRangeAndFilterFoundList(
    input: CharSequence,
    founds: SyntaxSegmentRanges
) =
    findAll(input).map { it.range }.filterFound(founds)
        .toList()

private fun Regex.findAllSegmentGroupRangeAndFilterFoundList(
    input: CharSequence,
    groupIndex: Int,
    founds: SyntaxSegmentRanges
) =
    findAll(input).map { it.groups[groupIndex]!!.range }
        .filterFound(founds).toList()

private fun IntRange.shiftLeft(offset: Int) =
    run { start - offset..endInclusive - offset }

private fun IntRange.shiftRight(offset: Int) =
    run { start + offset..endInclusive + offset }

private fun findAllFunctionSegmentGroupRangeAndFilterFoundList(
    input: CharSequence, founds: SyntaxSegmentRanges
): List<IntRange> =
    findAllFunctionSegmentGroupRangeAndFilterFound(
        input,
        founds
    ).toList()

private fun findAllFunctionSegmentGroupRangeAndFilterFound(
    input: CharSequence, founds: SyntaxSegmentRanges
): Sequence<IntRange> {
    val groups = functionRegex.findAll(input)
        .map(MatchResult::groups).toList()
    val functions =
        groups.asSequence().map { it[1]!!.range }
    val argumentGroups = groups.asSequence().map { it[2]!! }
    return if (argumentGroups.none()) functions
    else functions + argumentGroups.flatMap { group ->
        val offset = group.range.start
        findAllFunctionSegmentGroupRangeAndFilterFound(
            group.value,
            founds.map { it.shiftLeft(offset) }
        ).map { it.shiftRight(offset) }
    }
}

private fun Sequence<IntRange>.filterFound(founds: SyntaxSegmentRanges) =
    filter { new -> founds.all { found -> new doesNotOverlap found } }

private infix fun IntRange.doesNotOverlap(that: IntRange) =
    endInclusive < that.start || that.endInclusive < start

fun findCovscriptSyntaxSegmentss(text: String): CovscriptSyntaxSegmentss {
    val founds = mutableListOf<IntRange>()

    val comments =
        commentRegex.findAllSegmentRangeAndFilterFoundList(
            text,
            founds
        )
    founds += comments
    val stringLiterals =
        stringLiteralRegex.findAllSegmentRangeAndFilterFoundList(
            text,
            founds
        )
    founds += stringLiterals
    val numberLiterals =
        numberLiteralRegex.findAllSegmentRangeAndFilterFoundList(
            text,
            founds
        )
    founds += numberLiterals
    val preprocessingStatements =
        preprocessingStatementRegex.findAllSegmentRangeAndFilterFoundList(
            text,
            founds
        )
    founds += preprocessingStatements
    val keywords =
        keywordRegex.findAllSegmentGroupRangeAndFilterFoundList(
            text,
            1,
            founds
        )
    founds += keywords
    val symbols =
        symbolRegex.findAllSegmentRangeAndFilterFoundList(
            text,
            founds
        )
    founds += symbols
    val functions =
        findAllFunctionSegmentGroupRangeAndFilterFoundList(
            text,
            founds
        )
    founds += functions
    val variables =
        variableRegex.findAllSegmentRangeAndFilterFoundList(
            text,
            founds
        )

    return CovscriptSyntaxSegmentss(
        comments,
        stringLiterals,
        numberLiterals,
        preprocessingStatements,
        keywords,
        symbols,
        functions,
        variables
    )
}