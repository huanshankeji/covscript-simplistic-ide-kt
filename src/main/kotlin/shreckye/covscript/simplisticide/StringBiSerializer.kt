package shreckye.covscript.simplisticide

interface StringBiSerializer<Data> : StringSerializer<Data>, StringDeserializer<Data>

fun interface StringSerializer<Data> {
    fun dataToString(data: Data): String
}

fun interface StringDeserializer<Data> {
    fun stringToData(string: String): Data
}