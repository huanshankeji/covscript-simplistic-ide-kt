package shreckye.covscript.simplisticide

import java.io.File

val currentOSTerminalActions = runWhenCurrentOS(
    { LinuxXTerminalEmulatorActions },
    { MacOSXOpenTerminalActions },
    { WindowsCmdStartTerminalActions }
)


private fun String.quote() =
    "\"$this\""

private fun escapeCommand(command: String) =
    command.replace("\\", "\\\\").replace("\"", "\\\"")

private fun escapeCommandAndQuote(command: String): String =
    escapeCommand(command).quote()

private fun escapeCommandAndQuoteIfNeeded(command: String): String {
    val escapedCommand = escapeCommand(command)
    return if (!(command.startsWith('"') && command.endsWith('"')) &&
        command.contains(' ')
    ) escapedCommand.quote() else escapedCommand
}

private fun joinToCommandLine(vararg commands: String) =
    commands.asSequence().map(::escapeCommandAndQuoteIfNeeded).joinToString(" ")


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
        arrayOf("x-terminal-emulator", "-e", command).also { println(joinToCommandLine(*it)) }

    override fun getRunProcessWithTerminalCommands(vararg commands: String): Array<String> =
        arrayOf("x-terminal-emulator", "-e", joinToCommandLine(*commands)).also { println(joinToCommandLine(*it)) }

    override fun getRunProcessAndPauseWithTerminalCommands(vararg commands: String): Array<String> =
        getRunProcessWithTerminalCommands(
            *commands, ";",
            // "read" is a bash command instead of an executable
            "bash", "-c", joinToCommandLine("read", "-n1", "-r")
        )

    override fun getOpenTerminalCommands(): Array<String> =
        arrayOf("x-terminal-emulator")
}

object MacOSXOpenTerminalActions : TerminalActions {
    private fun escapeAndQuoteAppleScript(text: String) =
        // This solution may be buggy.
        escapeCommandAndQuote(text)

    private fun getOpenTerminalInDirectoryCommandsWithPossiblyMoreCommands(commands: Array<out String>?) =
        arrayOf(
            "bash", "-c",
            joinToCommandLine(
                "osascript", "-e",
                """tell app "Terminal" to do script ${
                    escapeAndQuoteAppleScript(joinToCommandLine("cd", "`pwd`",
                        *commands?.let { arrayOf(";", *it) } ?: emptyArray()))
                }"""
            )
        )

    override fun getRunNoArgProcessWithTerminalCommands(command: String): Array<String> =
        getRunProcessWithTerminalCommands(command)

    override fun getRunProcessWithTerminalCommands(vararg commands: String): Array<String> =
        getOpenTerminalInDirectoryCommandsWithPossiblyMoreCommands(
            arrayOf(*commands, ";", "exit")
        )

    override fun getRunProcessAndPauseWithTerminalCommands(vararg commands: String): Array<String> =
        getRunProcessWithTerminalCommands(*commands, ";", "read")

    override fun getOpenTerminalCommands(): Array<String> =
        getOpenTerminalInDirectoryCommandsWithPossiblyMoreCommands(null)
}

object WindowsCmdStartTerminalActions : TerminalActions {
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