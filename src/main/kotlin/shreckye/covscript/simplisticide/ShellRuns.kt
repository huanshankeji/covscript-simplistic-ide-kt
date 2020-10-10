package shreckye.covscript.simplisticide

import java.io.File

fun run(vararg commands: String, directory: File) =
    ProcessBuilder(*commands).directory(directory).start()

fun runAndPauseWithWindowsCmdWindow(vararg commands: String, directory: File) =
    run("cmd", "/c", "start", "cmd", "/c", *commands, "^&", "pause", directory = directory)