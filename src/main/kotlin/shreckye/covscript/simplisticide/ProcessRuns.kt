package shreckye.covscript.simplisticide

import java.io.File

fun startProcess(vararg commands: String) =
    ProcessBuilder(*commands).start()

fun startProcess(vararg commands: String, directory: File) =
    ProcessBuilder(*commands).directory(directory).start()