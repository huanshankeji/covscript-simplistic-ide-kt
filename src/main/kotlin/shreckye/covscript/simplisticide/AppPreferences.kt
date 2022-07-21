package shreckye.covscript.simplisticide

import javafx.scene.text.Font
import tornadofx.Component
import java.nio.charset.Charset
import java.util.prefs.Preferences

val NODE_NAME = PACKAGE_NAME
fun Component.appPreferences(op: Preferences.() -> Unit) =
    preferences(NODE_NAME, op)

const val SDK_PATH_KEY = "sdk path"
val defaultSdkPath: String? = null

const val LINE_SEPARATOR_KEY = "line separator"
val defaultLineSeparator get() = systemLineSeparator
fun LineSeparator?.orDefault() =
    this ?: defaultLineSeparator

val lineSeparators = LineSeparator.values().asList()
val lineSeparatorsWithNullForDefault =
    listOf(null) + lineSeparators

const val FILE_ENCODING_KEY = "file encoding"
val defaultFileEncoding: Charset get() = Charset.defaultCharset()
fun Charset?.orFileEncodingDefault() =
    this ?: defaultFileEncoding

val fileEncodings get() = Charset.availableCharsets().values.toList()
val fileEncodingsWithNullForDefault get() = listOf(null) + fileEncodings

const val INDENTATION_KEY = "indentation"

/*const val DEFAULT_SPACES_NUMBER = 4
fun Int?.orDefaultSpacesNumber() = this?: DEFAULT_SPACES_NUMBER*/
val defaultIndentation = Indentation.Spaces(4)
fun Indentation?.orDefault() = this ?: defaultIndentation
val indentationTypes = Indentation::class.sealedSubclasses
val indentationTypesWithNullForDefault =
    listOf(null) + indentationTypes

const val FONT_SIZE_KEY = "font size"
val defaultFontSize get() = Font.getDefault().size
fun Double?.orFontSizeDefault() = this ?: defaultFontSize


class AppPreferences(
    val sdkPath: String?,
    val lineSeparator: LineSeparator?,
    val fileEncoding: Charset?,
    val indentation: Indentation?,
    val fontSize: Double?
)

val nullForDefaultAppPreferences
    get() = AppPreferences(null, null, null, null, null)

/*
val defaultAppPreferences
    get() = AppPreferences(defaultSdkPath, defaultLineSeparator, defaultFileEncoding, defaultIndentation, defaultFontSize)*/
