package shreckye.covscript.simplisticide

import javafx.beans.property.SimpleObjectProperty
import tornadofx.Commit
import tornadofx.ViewModel
import java.nio.charset.Charset

class Options(
    val sdkPath: String?,
    val lineSeparator: LineSeparator?, val fileEncoding: Charset?, val indentation: Indentation?, val fontSize: Double?
)

interface IOptionsProperties {
    val sdkPathProperty: SimpleObjectProperty<String?>

    val lineSeparatorProperty: SimpleObjectProperty<LineSeparator?>
    val fileEncodingProperty: SimpleObjectProperty<Charset?>
    val indentationProperty: SimpleObjectProperty<Indentation?>
    val fontSizeProperty: SimpleObjectProperty<Double?>

    fun getAll() = Options(
        sdkPathProperty.get(),
        lineSeparatorProperty.get(), fileEncodingProperty.get(), indentationProperty.get(), fontSizeProperty.get()
    )

    fun setAll(options: Options) = with(options) {
        sdkPathProperty.set(sdkPath)

        lineSeparatorProperty.set(lineSeparator)
        fileEncodingProperty.set(fileEncoding)
        indentationProperty.set(indentation)
        fontSizeProperty.set(fontSize)
    }
}

data class OptionsProperties(
    override val sdkPathProperty: SimpleObjectProperty<String?> = SimpleObjectProperty<String?>(),

    override val lineSeparatorProperty: SimpleObjectProperty<LineSeparator?> = SimpleObjectProperty(),
    override val fileEncodingProperty: SimpleObjectProperty<Charset?> = SimpleObjectProperty(),
    override val indentationProperty: SimpleObjectProperty<Indentation?> = SimpleObjectProperty(),
    override val fontSizeProperty: SimpleObjectProperty<Double?> = SimpleObjectProperty()
) : IOptionsProperties

fun Options.toProperties() = OptionsProperties().apply { setAll(this@toProperties) }

// TODO: Encapsulate for saving, avoid changing without saving
class OptionsVM : ViewModel(), IOptionsProperties by OptionsProperties() {
    init {
        preferences(NODE_NAME) {
            sdkPathProperty.set(get(SDK_PATH_KEY, null))

            lineSeparatorProperty.set(getObject(LINE_SEPARATOR_KEY, LineSeparator))
            fileEncodingProperty.set(getObject(FILE_ENCODING_KEY, CharsetBiSerializer))
            indentationProperty.set(getObject(INDENTATION_KEY, Indentation))
            fontSizeProperty.set(getDoubleOrNull(FONT_SIZE_KEY))
        }
    }

    override fun onCommit(commits: List<Commit>) {
        preferences {
            putOrRemove(SDK_PATH_KEY, sdkPathProperty.get())

            putOrRemoveObject(LINE_SEPARATOR_KEY, lineSeparatorProperty.get(), LineSeparator)
            putOrRemoveObject(FILE_ENCODING_KEY, fileEncodingProperty.get(), CharsetBiSerializer)
            putOrRemoveObject(INDENTATION_KEY, indentationProperty.get(), Indentation)
            putOrRemoveDouble(FONT_SIZE_KEY, fontSizeProperty.get())
        }
    }
}

fun OptionsVM.copyToProperties() =
    getAll().toProperties()