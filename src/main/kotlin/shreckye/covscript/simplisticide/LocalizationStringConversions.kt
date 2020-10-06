package shreckye.covscript.simplisticide

import shreckye.covscript.simplisticide.javafx.ToStringOnlyStringConverter

inline fun <T> toEnglishStringWithNullForDefault(default: String, value: T?, notNullToString: (T) -> String) =
    if (value === null) "default: $default" else notNullToString(value)

inline fun <T> toEnglishStringWithNullForDefault(default: T, value: T?, notNullToString: (T) -> String) =
    toEnglishStringWithNullForDefault(notNullToString(default), value, notNullToString)

inline fun <T> toEnglishStringOnlyConverterWithNullForDefault(default: String, crossinline notNullToString: (T) -> String) =
    object : ToStringOnlyStringConverter<T?>() {
        override fun toString(`object`: T?): String =
            toEnglishStringWithNullForDefault(default, `object`, notNullToString)
    }

inline fun <T> toEnglishStringOnlyConverterWithNullForDefault(default: T, crossinline notNullToString: (T) -> String) =
    toEnglishStringOnlyConverterWithNullForDefault(notNullToString(default), notNullToString)