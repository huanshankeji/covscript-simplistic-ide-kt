package shreckye.covscript

import shreckye.covscript.simplisticide.runWhenCurrentOSIsPosixOrWindows

const val COVSCRIPT_FULL_NAME =
    "The Covariant Script Programming Language"
const val COVSCRIPT_ICON_WIDE_URL =
    "https://github.com/covscript/covscript/raw/master/icon/covariant_script_wide.png"
const val COVSCRIPT_HOMEPATE_URL = "http://covscript.org.cn"
const val COVSCRIPT_GITHUB_URL =
    "https://github.com/covscript/covscript"

object CovScriptSdkDirectory {
    // Copied from ProgramSettings.cs
    fun String.toExecutableName(): String =
        runWhenCurrentOSIsPosixOrWindows(
            { this },
            { "$this.exe" })

    object BinDirectory {
        const val NAME = "bin"

        val cs = "cs".toExecutableName()

        //val csRepl = "cs".toExecutableName()
        val csDbg = "cs_dbg".toExecutableName()
        val csInst = "cs_inst".toExecutableName()
        const val csLog = "cs_gui.log"
    }

    const val IMPORTS_DIRECTORY = "imports"
    const val DOCS_DIRECTORY = "docs"
}