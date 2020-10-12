package shreckye.covscript

const val COVSCRIPT_FULL_NAME = "The Covariant Script Programming Language"
const val COVSCRIPT_ICON_WIDE_URL = "https://github.com/covscript/covscript/raw/master/icon/covariant_script_wide.png"
const val COVSCRIPT_HOMEPATE_URL = "http://covscript.org.cn"
const val COVSCRIPT_GITHUB_URL = "https://github.com/covscript/covscript"

object CovScriptSdkDirectory {
    // Copied from ProgramSettings.cs
// Currently Windows only supported
    object BinDirectory {
        const val NAME = "bin"

        const val cs = "cs.exe"
        const val csRepl = "cs.exe"
        const val csDbg = "cs_dbg.exe"
        const val csInst = "cs_inst.exe"
        const val csLog = "cs_gui.log"
    }

    const val IMPORTS_DIRECTORY = "imports"
    const val DOCS_DIRECTORY = "docs"
}