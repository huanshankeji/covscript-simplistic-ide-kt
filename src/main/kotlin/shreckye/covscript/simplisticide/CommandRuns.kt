package shreckye.covscript.simplisticide

import java.io.File

fun startProcess(vararg commands: String) =
    ProcessBuilder(*commands).start()

fun startProcess(vararg commands: String, directory: File) =
    ProcessBuilder(*commands).directory(directory).start()

private val runtime = Runtime.getRuntime()
fun runtimeExec(command: String) =
    runtime.exec(command)

fun runtimeExec(command: String, directory: File) =
    runtime.exec(command, null, directory)