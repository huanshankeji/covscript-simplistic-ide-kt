package shreckye.covscript.simplisticide

import java.io.File

fun runProcessWithWindowsCmdWindow(filename: String) =
    runProcess("cmd", "/c", "start", filename)

fun runProcessWithWindowsCmdWindow(filename: String, directory: File) =
    runProcess("cmd", "/c", "start", filename, directory = directory)

fun runProcessAndPauseWithWindowsCmdWindow(vararg commands: String, directory: File) =
    runProcess("cmd", "/c", "start", "cmd", "/c", *commands, "^&", "pause", directory = directory)

fun openWindowsCmdWindow() =
    runProcessWithWindowsCmdWindow("cmd")

fun openWindowsCmdWindow(directory: File) =
    runProcessWithWindowsCmdWindow("cmd", directory)