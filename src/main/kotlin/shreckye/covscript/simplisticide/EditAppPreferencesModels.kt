package shreckye.covscript.simplisticide

import javafx.beans.property.SimpleObjectProperty
import java.nio.charset.Charset

fun Indentation.type() = this::class
fun Indentation.numberOrNull() =
    (this as? Indentation.Spaces)?.number

fun indentationFromTypeAndNumber(
    type: IndentationType,
    number: Int?
): Indentation =
    when (type) {
        Indentation.Spaces::class -> Indentation.Spaces(
            number!!
        )
        Indentation.Tab::class -> Indentation.Tab
        else -> throw IllegalArgumentException()
    }

fun indentationFromTypeWithNullForDefaultAndNumber(
    type: IndentationType?,
    number: Int?
): Indentation? =
    type?.let { indentationFromTypeAndNumber(it, number) }

/*fun tryIndentationFromTypeWithNullForDefaultAndNumber(type: KClass<out Indentation>?, number: Int?): Try<Indentation?> =
    tryBlock { indentationFromTypeWithNullForDefaultAndNumber(type, number) }*/

interface IEditAppPreferenceProperties {
    val sdkPathProperty: SimpleObjectProperty<String?>

    val lineSeparatorProperty: SimpleObjectProperty<LineSeparator?>
    val fileEncodingProperty: SimpleObjectProperty<Charset?>

    //val tryIndentationProperty: SimpleObjectProperty<Try<Indentation?>>
    val indentationTypeProperty: SimpleObjectProperty<IndentationType?>
    val indentationNumberProperty: SimpleObjectProperty<Int?>
    val fontSizeProperty: SimpleObjectProperty<Double?>

    fun getAll() = AppPreferences(
        sdkPathProperty.get(),

        lineSeparatorProperty.get(),
        fileEncodingProperty.get(),
        indentationFromTypeWithNullForDefaultAndNumber(
            indentationTypeProperty.get(),
            indentationNumberProperty.get()
        ),
        fontSizeProperty.get()
    )

    fun setAll(appPreferences: AppPreferences) =
        with(appPreferences) {
            sdkPathProperty.set(sdkPath)

            lineSeparatorProperty.set(lineSeparator)
            fileEncodingProperty.set(fileEncoding)
            with(appPreferences.indentation) {
                indentationTypeProperty.set(this?.type())
                indentationNumberProperty.set(this?.numberOrNull())
            }
            fontSizeProperty.set(fontSize)
        }

    fun setAllNullForDefault() =
        setAll(nullForDefaultAppPreferences)
}

data class EditAppPreferenceProperties(
    override val sdkPathProperty: SimpleObjectProperty<String?> = SimpleObjectProperty<String?>(),

    override val lineSeparatorProperty: SimpleObjectProperty<LineSeparator?> = SimpleObjectProperty(),
    override val fileEncodingProperty: SimpleObjectProperty<Charset?> = SimpleObjectProperty(),

    override val indentationTypeProperty: SimpleObjectProperty<IndentationType?> = SimpleObjectProperty(),
    override val indentationNumberProperty: SimpleObjectProperty<Int?> = SimpleObjectProperty(),
    override val fontSizeProperty: SimpleObjectProperty<Double?> = SimpleObjectProperty()
) : IEditAppPreferenceProperties

fun AppPreferences.toEditProperties() =
    EditAppPreferenceProperties().apply { setAll(this@toEditProperties) }

fun AppPreferencesVM.copyToEditProperties() =
    getAll().toEditProperties()