package shreckye.covscript.simplisticide

interface StringSerializer<Data> {
    fun dataToString(data: Data): String
    fun stringToData(string: String): Data
}