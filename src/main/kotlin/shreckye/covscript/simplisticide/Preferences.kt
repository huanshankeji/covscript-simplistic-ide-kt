package shreckye.covscript.simplisticide

import javafx.scene.text.Font
import java.nio.charset.Charset
import java.util.prefs.Preferences
import kotlin.reflect.KClass

const val NODE_NAME = PACKAGE_NAME

const val SDK_PATH_KEY = "sdk path"

const val LINE_SEPARATOR_KEY = "line separator"
val defaultLineSeparator get() = systemLineSeparator
fun LineSeparator?.orDefault() = this ?: defaultLineSeparator
val lineSeparators = LineSeparator.values().asList()
val lineSeparatorsWithNullForDefault = listOf(null) + lineSeparators

const val FILE_ENCODING_KEY = "file encoding"
val DEFAULT_FILE_ENCODING: Charset = Charset.defaultCharset()
fun Charset?.orFileEncodingDefault() = this ?: DEFAULT_FILE_ENCODING
val fileEncodings get() = Charset.availableCharsets().values.toList()
val fileEncodingsWithNullForDefault get() = listOf(null) + fileEncodings

const val INDENTATION_KEY = "indentation"
/*const val DEFAULT_SPACES_NUMBER = 4
fun Int?.orDefaultSpacesNumber() = this?: DEFAULT_SPACES_NUMBER*/
val DEFAULT_INDENTATION = Indentation.Spaces(4)
fun Indentation?.orDefault() = this ?: DEFAULT_INDENTATION
val indentationTypes = Indentation::class.sealedSubclasses
val indentationTypesWithNullForDefault = listOf(null) + indentationTypes
fun indentationFromTypeAndNumber(type: KClass<out Indentation>, number: Int) =
    when (type) {
        Indentation.Spaces::class -> Indentation.Spaces(number)
        Indentation.Tab::class -> Indentation.Tab
        else -> throw IllegalArgumentException()
    }

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