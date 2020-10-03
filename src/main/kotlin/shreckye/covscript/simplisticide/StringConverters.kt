package shreckye.covscript.simplisticide

import shreckye.covscript.simplisticide.javafx.util.ToStringOnlyStringConverter

inline fun <T> toStringOnlyConverterWithNullForDefault(default: String, crossinline notNullToString: (T) -> String) =
    object : ToStringOnlyStringConverter<T?>() {
        override fun toString(`object`: T?): String =
            if (`object` === null) "default: $default" else notNullToString(`object`)
    }

inline fun <T> toStringOnlyConverterWithNullForDefault(default: T, crossinline notNullToString: (T) -> String) =
    shreckye.covscript.simplisticide.toStringOnlyConverterWithNullForDefault(notNullToString(default), notNullToString)