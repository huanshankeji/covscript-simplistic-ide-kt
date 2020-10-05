package shreckye.covscript.simplisticide

import shreckye.covscript.simplisticide.javafx.util.ToStringOnlyStringConverter

inline fun <T> toStringWithNullForDefault(default: String, value: T?, notNullToString: (T) -> String) =
    if (value === null) "default: $default" else notNullToString(value)

inline fun <T> toStringWithNullForDefault(default: T, value: T?, notNullToString: (T) -> String) =
    toStringWithNullForDefault(notNullToString(default), value, notNullToString)

inline fun <T> toStringOnlyConverterWithNullForDefault(default: String, crossinline notNullToString: (T) -> String) =
    object : ToStringOnlyStringConverter<T?>() {
        override fun toString(`object`: T?): String =
            toStringWithNullForDefault(default, `object`, notNullToString)
    }

inline fun <T> toStringOnlyConverterWithNullForDefault(default: T, crossinline notNullToString: (T) -> String) =
    toStringOnlyConverterWithNullForDefault(notNullToString(default), notNullToString)