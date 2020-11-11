package shreckye.covscript.simplisticide

import java.io.File

private val osName = System.getProperty("os.name")

val currentOSTerminalActions = when {
    osName.startsWith("Linux") -> LinuxXTerminalEmulatorActions
    osName.startsWith("Mac OS X") -> MacOSXOpenTerminalActions
    osName.startsWith("Windows") -> WindowsCmdWindowActions
    else -> throw IllegalArgumentException("unknown OS name: $osName")
}

interface TerminalActions {
    fun getRunNoArgProcessWithTerminalCommands(command: String): Array<String>


    fun runProcessWithTerminal(vararg commands: String): Process =
        startProcess(*getRunProcessWithTerminalCommands(*commands))

    fun runProcessWithTerminal(vararg commands: String, directory: File): Process =
        startProcess(*getRunProcessWithTerminalCommands(*commands), directory = directory)

    fun getRunProcessWithTerminalCommands(vararg commands: String): Array<String>


    fun runProcessAndPauseWithTerminal(vararg commands: String): Process =
        startProcess(*getRunProcessAndPauseWithTerminalCommands(*commands))

    fun runProcessAndPauseWithTerminal(vararg commands: String, directory: File): Process =
        startProcess(*getRunProcessAndPauseWithTerminalCommands(*commands), directory = directory)

    fun getRunProcessAndPauseWithTerminalCommands(vararg commands: String): Array<String>


    fun openTerminal(): Process =
        startProcess(*getOpenTerminalCommands())

    fun openTerminal(directory: File): Process =
        startProcess(*getOpenTerminalCommands(), directory = directory)

    fun getOpenTerminalCommands(): Array<String>
}

object LinuxXTerminalEmulatorActions : TerminalActions {
    override fun getRunNoArgProcessWithTerminalCommands(command: String): Array<String> =
        TODO()

    override fun getRunProcessWithTerminalCommands(vararg commands: String): Array<String> =
        arrayOf("x-terminal-emulator", "-e", *commands)

    override fun getRunProcessAndPauseWithTerminalCommands(vararg commands: String): Array<String> =
        getRunProcessWithTerminalCommands(*commands, "&", "read", "-n1", "-r")

    override fun getOpenTerminalCommands(): Array<String> =
        arrayOf("x-terminal-emulator")
}

object MacOSXOpenTerminalActions : TerminalActions {
    override fun getRunNoArgProcessWithTerminalCommands(command: String): Array<String> =
        TODO()

    override fun getRunProcessWithTerminalCommands(vararg commands: String): Array<String> =
        arrayOf(
            "osascript", "-e",
            """'tell app "Terminal" to do script "${
                commands.joinToString(" ") {
                    val doubleQuotesEscaped = it.replace("\"", "\\\"")
                    if (doubleQuotesEscaped.contains(' ')) "\"$doubleQuotesEscaped\"" else doubleQuotesEscaped
                }
            }"'"""
        )

    override fun getRunProcessAndPauseWithTerminalCommands(vararg commands: String): Array<String> =
        getRunProcessWithTerminalCommands(*commands, "&", "read")

    override fun getOpenTerminalCommands(): Array<String> =
        arrayOf("open", "-na", "Terminal")
}

object WindowsCmdWindowActions : TerminalActions {
    // "start" is a CMD command instead of an executable

    override fun getRunNoArgProcessWithTerminalCommands(command: String): Array<String> =
        arrayOf("cmd", "/c", "start", command)

    override fun getRunProcessWithTerminalCommands(vararg commands: String): Array<String> =
        arrayOf("cmd", "/c", "start", "cmd", "/c", *commands)

    override fun getRunProcessAndPauseWithTerminalCommands(vararg commands: String): Array<String> =
        getRunProcessWithTerminalCommands(*commands, "^&", "pause")

    override fun getOpenTerminalCommands(): Array<String> =
        getRunNoArgProcessWithTerminalCommands("cmd")
}