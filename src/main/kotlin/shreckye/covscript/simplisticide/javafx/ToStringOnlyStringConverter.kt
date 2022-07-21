package shreckye.covscript.simplisticide.javafx

import javafx.util.StringConverter

abstract class ToStringOnlyStringConverter<T> :
    StringConverter<T>() {
    abstract override fun toString(`object`: T): String
    override fun fromString(string: String?): T =
        throw AssertionError()
}