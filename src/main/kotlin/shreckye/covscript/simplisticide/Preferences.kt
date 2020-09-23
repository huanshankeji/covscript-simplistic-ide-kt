package shreckye.covscript.simplisticide

import javafx.scene.text.Font
import java.nio.charset.Charset
import java.util.prefs.Preferences

const val NODE_NAME = PACKAGE_NAME

const val SDK_PATH_KEY = "sdk path"

const val LINE_SEPARATOR_KEY = "line separator"
val DEFAULT_LINE_SEPARATOR = SYSTEM_LINE_SEPARATOR
fun LineSeparator?.orDefault() = this ?: DEFAULT_LINE_SEPARATOR

const val FILE_ENCODING_KEY = "file encoding"
val DEFAULT_FILE_ENCODING: Charset = Charset.defaultCharset()
fun Charset?.orFileEncodingDefault() = this ?: DEFAULT_FILE_ENCODING

const val INDENTATION_KEY = "indentation"
val DEFAULT_INDENTATION = Indentation.Spaces(4)
fun Indentation?.orDefault() = this ?: DEFAULT_INDENTATION

const val FONT_SIZE_KEY = "font size"
val DEFAULT_FONT_SIZE = Font.getDefault().size
fun Double?.orFontSizeDefault() = this ?: DEFAULT_FONT_SIZE


fun Preferences.getOrNull(key: String) =
    get(key, null)

fun Preferences.putOrRemove(key: String, value: String?) =
    if (value !== null) put(key, value) else remove(key)

/*fun <Data> Preferences.getObject(key: String, def: String, deserializer: StringDeserializer<Data>) =
    deserializer.stringToData(get(key, def))*/

fun <Data> Preferences.getObject(key: String, deserializer: StringDeserializer<Data>, default: Data? = null) =
    get(key, null)?.let(deserializer::stringToData) ?: default

fun <Data> Preferences.putObject(key: String, data: Data, serializer: StringSerializer<Data>) =
    put(key, serializer.dataToString(data))

fun <Data> Preferences.putOrRemoveObject(key: String, data: Data?, serializer: StringSerializer<Data>) =
    if (data !== null) putObject(key, data, serializer) else remove(key)

fun Preferences.getDoubleOrNull(key: String) =
    get(key, null)?.let { getDouble(key, 0.0) }

fun Preferences.putOrRemoveDouble(key: String, value: Double?) =
    if (value !== null) putDouble(key, value) else remove(key)