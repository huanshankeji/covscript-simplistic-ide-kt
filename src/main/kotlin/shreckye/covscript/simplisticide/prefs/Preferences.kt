package shreckye.covscript.simplisticide.prefs

import shreckye.covscript.simplisticide.StringDeserializer
import shreckye.covscript.simplisticide.StringSerializer
import java.util.prefs.Preferences

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