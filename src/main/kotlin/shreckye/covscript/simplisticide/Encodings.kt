package shreckye.covscript.simplisticide

import java.nio.charset.Charset

val DEFAULT_CHARSET: Charset = Charset.defaultCharset()

object CharsetBiSerializer : StringBiSerializer<Charset> {
    override fun dataToString(data: Charset): String = data.name()
    override fun stringToData(string: String): Charset = Charset.forName(string)
}

fun Charset.serializeToString() = CharsetBiSerializer.dataToString(this)
fun String.deserializeToCharset() = CharsetBiSerializer.stringToData(this)