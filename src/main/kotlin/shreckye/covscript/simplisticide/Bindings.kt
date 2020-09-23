package shreckye.covscript.simplisticide

import com.sun.javafx.binding.BidirectionalBinding
import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

fun <T> Property<T?>.bindToNonnullWithDefault(default: T, propertyCreator: () -> Property<T>): Property<T> =
   TODO()

fun <T> SimpleObjectProperty<T?>.bindToNonnullWithDefault(default: T): SimpleObjectProperty<T> =
    TODO()

fun SimpleObjectProperty<String?>.bindToNonnullWithDefault(default: String): SimpleStringProperty =
    TODO()