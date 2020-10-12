package shreckye.covscript.simplisticide

import java.io.File

fun runProcess(vararg commands: String) =
    ProcessBuilder(*commands).start()

fun runProcess(vararg commands: String, directory: File) =
    ProcessBuilder(*commands).directory(directory).start()