package shreckye.covscript.simplisticide

import java.awt.Desktop
import java.io.File

fun desktopOpen(file: File) =
    Desktop.getDesktop().open(file)

/* doesn't work
fun desktopBrowseFileDirectory(file: File) =
    Desktop.getDesktop().browseFileDirectory(file)*/
