package shreckye.covscript.simplisticide

import javafx.stage.FileChooser

val CS_PACKAGE_FILE_EXTENSION_FILTER =
    FileChooser.ExtensionFilter(
        "CovScript package file",
        "*.csp"
    )

val SAVE_FILE_FILTERS = arrayOf(
    FileChooser.ExtensionFilter(
        "CovScript source code",
        "*.csc"
    ),

    )

val AST_FILE_FILTERS = arrayOf(
    FileChooser.ExtensionFilter(
        "CovScript AST file",
        "*.csa"
    )
)

val EXTENSION_FILE_FILTERS = arrayOf(
    FileChooser.ExtensionFilter(
        "CovScript extension",
        "*.cse"
    ),
    CS_PACKAGE_FILE_EXTENSION_FILTER
)

val javafxLineSeparator = LineSeparator.LF