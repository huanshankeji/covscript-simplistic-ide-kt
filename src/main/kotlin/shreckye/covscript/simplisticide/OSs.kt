package shreckye.covscript.simplisticide

val currentOSName = System.getProperty("os.name")

inline fun <R> runWhenCurrentOS(
    whenLinux: () -> R,
    whenMacOSX: () -> R,
    whenWindows: () -> R
): R =
    when {
        currentOSName.startsWith("Linux") -> whenLinux()
        currentOSName.startsWith("Mac OS X") -> whenMacOSX()
        currentOSName.startsWith("Windows") -> whenWindows()
        else -> throw IllegalArgumentException("unknown OS name: $currentOSName")
    }

val isPosixOrWindows =
    runWhenCurrentOS({ true }, { true }, { false })

inline fun <R> runWhenCurrentOSIsPosixOrWindows(
    whenPosix: () -> R,
    whenWindows: () -> R
): R =
    if (isPosixOrWindows) whenPosix() else whenWindows()