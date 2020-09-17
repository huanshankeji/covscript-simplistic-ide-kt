package shreckye.covscript.simplisticide

import javafx.scene.text.Font
import java.util.prefs.Preferences

const val NODE_NAME = PACKAGE_NAME

const val SDK_PATH_KEY = "sdk path"

const val LINE_SEPARATOR_KEY = "line separator"
val DEFAULT_LINE_SEPARATOR = SYSTEM_LINE_SEPARATOR

const val FILE_ENCODING_KEY = "file encoding"
val DEFAULT_FILE_ENCODING = DEFAULT_CHARSET

const val INDENTATION_KEY = "indentation"
val DEFAULT_INDENTATION = Indentation.DEFAULT

const val FONT_SIZE_KEY = "font size"
val DEFAULT_FONT_SIZE = Font.getDefault().size

/*fun <Data> Preferences.getWithStringDeserializer(key: String, def: String, deserializer: StringDeserializer<Data>) =
    deserializer.stringToData(get(key, def))*/
fun <Data> Preferences.getObject(key: String, deserializer: StringDeserializer<Data>, default: Data? = null) =
    get(key, null)?.let(deserializer::stringToData) ?: default

fun <Data> Preferences.putObject(key: String, data: Data, serializer: StringSerializer<Data>) =
    put(key, serializer.dataToString(data))

fun <Data> Preferences.putOrRemoveObject(key: String, data: Data?, serializer: StringSerializer<Data>) =
    if (data === null) remove(key) else putObject(key, data, serializer)

fun Preferences.getDoubleOrNull(key: String) = get(key, null)?.let { getDouble(key, 0.0) }