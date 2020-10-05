package shreckye.covscript.simplisticide

import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue

// Adapted from tornadofx.onChange
fun <T> ObservableValue<T>.onChange(op: (T) -> Unit) =
    addListener { _, _, newValue -> op(newValue) }

fun <T> ObservableValue<T>.bindByOnChange(op: (T) -> Unit) {
    op(value)
    onChange(op)
}

interface Converter<A, B> {
    fun aToB(a: A): B
    fun bToA(b: B): A
}

// Simply bind bidirectionally for all types
fun <A, B> bindBidirectionally(propertyA: Property<A>, propertyB: Property<B>, converter: Converter<A, B>) {
    // The JavaFX implementations use `WeakReference` to prevent memory leaking
    // and an `updating` flag to prevent cyclic updates
    propertyA.onChange { propertyB.value = converter.aToB(it) }
    propertyB.onChange { propertyA.value = converter.bToA(it) }
}


fun <A, B, P : Property<B>> Property<A>.bidirectionalBinding(
    propertyCreator: () -> P, converter: Converter<A, B>
): P =
    propertyCreator().also { bindBidirectionally(this, it, converter) }

fun <A, B> Property<A>.bidirectionalBinding(converter: Converter<A, B>) =
    bidirectionalBinding(::SimpleObjectProperty, converter)

fun <A> Property<A>.bidirectionalBinding(converter: Converter<A, String>) =
    bidirectionalBinding(::SimpleStringProperty, converter)

fun <T, P : Property<T>> Property<T?>.bindingWithNullForDefault(propertyCreator: () -> P, default: T): P =
    bidirectionalBinding(propertyCreator, object : Converter<T?, T> {
        override fun aToB(a: T?): T = a ?: default
        override fun bToA(b: T): T? = if (b == default) null else b
    })

fun <T> SimpleObjectProperty<T?>.bindingWithNullForDefault(default: T): SimpleObjectProperty<T> =
    bindingWithNullForDefault(::SimpleObjectProperty, default)

fun SimpleObjectProperty<String?>.bindingWithNullForDefault(default: String): SimpleStringProperty =
    bindingWithNullForDefault(::SimpleStringProperty, default)