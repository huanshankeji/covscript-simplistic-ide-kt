package shreckye.covscript.simplisticide

import javafx.beans.property.SimpleObjectProperty
import kotlin.reflect.KClass
// TODO
val properties = object {
    /*val indentationTypeProperty: SimpleObjectProperty<KClass<out Indentation>?> =
        indentationProperty.bidirectionalBinding(object :
            Converter<Indentation?, KClass<out Indentation>?> {
            override fun aToB(a: Indentation?): KClass<out Indentation>? =
                a?.let { it::class }

            override fun bToA(b: KClass<out Indentation>?): Indentation? =
                indentationFromTypeWithNullForDefaultAndNumber(b, numberProperty.get())
        })
    val numberProperty =
        indentationProperty.bidirectionalBinding(object : Converter<Indentation?, Int?> {
            override fun aToB(a: Indentation?): Int? =
                (a as? Indentation.Spaces)?.number

            override fun bToA(b: Int?): Indentation? =
                indentationFromTypeWithNullForDefaultAndNumber(indentationTypeProperty.get(), b)
        })*/
}