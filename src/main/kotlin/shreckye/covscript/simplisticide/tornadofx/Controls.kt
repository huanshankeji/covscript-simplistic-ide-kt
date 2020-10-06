package shreckye.covscript.simplisticide.tornadofx

import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.scene.control.TextField
import javafx.util.StringConverter
import tornadofx.textfield

@JvmName("textfieldNullableInt")
fun EventTarget.textfield(property: Property<Int?>, op: TextField.() -> Unit = {}) =
    @Suppress("UNCHECKED_CAST") textfield(property as Property<Int>, op)

@JvmName("textfieldNullableDouble")
fun EventTarget.textfield(property: Property<Double?>, op: TextField.() -> Unit = {}) =
    textfield(
        property,
        object : StringConverter<Double?>() {
            override fun toString(`object`: Double?): String =
                `object`?.toString() ?: ""

            override fun fromString(string: String): Double? =
                if (string.isNotEmpty()) string.toDouble() else null
        },
        op
    )