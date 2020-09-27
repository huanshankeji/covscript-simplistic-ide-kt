package shreckye.covscript.simplisticide

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea
import javafx.scene.layout.AnchorPane.setLeftAnchor
import javafx.scene.layout.AnchorPane.setRightAnchor
import javafx.util.StringConverter
import tornadofx.*
import java.io.File
import kotlin.reflect.KClass

class MainApp : App(MainFragment::class)

class MainFragment : Fragment(APP_NAME) {
    val fileProperty = SimpleObjectProperty<File?>()
    val savedContentProperty = SimpleStringProperty()
    val contentProperty = SimpleStringProperty()
    val optionsView by inject<OptionsVM>()

    lateinit var contentTextArea: TextArea

    fun init(file: File?) {
        fileProperty.set(file)
        if (file === null)
            initContent("")
        else
            initContent(file.readText())
    }

    fun initContent(contentValue: String) {
        savedContentProperty.set(contentValue)
        contentProperty.set(contentValue)
    }

    fun initNew() = init(null)
    fun saveWarningIfEdited(continuation: () -> Unit = {}, cancelContinuation: () -> Unit = {}) {
        if (contentProperty.get() != savedContentProperty.get())
            alert(
                Alert.AlertType.WARNING, "The file has been edited. Save it?",
                buttons = arrayOf(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL),
                owner = currentWindow
            ) {
                when (it) {
                    ButtonType.YES -> {
                        save()
                        continuation()
                    }
                    ButtonType.NO -> continuation()
                    ButtonType.CANCEL -> cancelContinuation()
                }
            }
        else continuation()
    }

    // TODO: EOL
    fun save() {
        val file = fileProperty.get()
        if (file === null) saveAs() else saveAs(file)
    }

    fun saveAs() {
        val file =
            chooseFile("Save As...", FILE_FILTERS, mode = FileChooserMode.Save, owner = currentWindow).getOrNull(0)
        file?.let {
            fileProperty.set(it)
            saveAs(it)
        }
    }

    fun saveAs(file: File) {
        file.writeText(contentProperty.get())
        savedContentProperty.set(contentProperty.get())
    }

    init {
        initNew()
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        // TODO: prevent shutdown with unsaved changes
        currentWindow!!.setOnCloseRequest {
            saveWarningIfEdited(cancelContinuation = it::consume)
        }
    }

    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("New", "Ctrl+N") {
                    action {
                        alert(
                            Alert.AlertType.CONFIRMATION, "Open in a new window?",
                            buttons = arrayOf(ButtonType.YES, ButtonType.NO),
                            owner = currentWindow
                        ) {
                            when (it) {
                                ButtonType.YES -> find<MainFragment>().openWindow(owner = null)
                                ButtonType.NO -> {
                                    saveWarningIfEdited {
                                        initNew()
                                    }
                                }
                            }
                        }
                    }
                }
                item("Open...", "Ctrl+O") {
                    action {
                        saveWarningIfEdited {
                            val file = chooseFile("Open...", FILE_FILTERS, owner = currentWindow).getOrNull(0)
                            file?.let { init(it) }
                        }
                    }
                }
                separator()
                item("Save", "Ctrl+S") { action { save() } }
                item("Save As...", "Ctrl+Shift+S") { action { saveAs() } }
                separator()
                item("Exit") { action { saveWarningIfEdited { close() } } }
            }

            menu("Edit") {
                // Lambdas can't be replaced with references here for the var `contentTextArea`
                item("Undo", "Ctrl+Z") { action { contentTextArea.undo() } }
                item("Redo", "Ctrl+Shift+Z") { action { contentTextArea.redo() } }
                separator()
                item("Cut", "Ctrl+X") { action { contentTextArea.cut() } }
                item("Copy", "Ctrl+C") { action { contentTextArea.copy() } }
                item("Paste", "Ctrl+V") { action { contentTextArea.paste() } }
                item("Select All", "Ctrl+A") { action { contentTextArea.selectAll() } }
            }

            menu("Tools") {
                item("Run", "Ctrl+R") { action { TODO() } }
                item("Run with Options...") { action { } }
                item("Run Debugger")
                separator()
                // TODO
                item("Shell")
                item("View Error Info")
                item("Run Installer")
                item("REPL")
                separator()
                item("Build Independent Executable...")
                item("Install Extensions...")
                item("Options...") { action { find<OptionsFragment>().openModal() } }
            }

            menu("Help") {
                item("About $APP_NAME")
                item("SDK Version Info")
                separator()
                item("Visit CovScript Homepage")
                item("View Documentation")
            }
        }

        center = textarea(contentProperty) {
            font.size
            //fontProperty().select { font.getProperty() }
        }
            .also { contentTextArea = it }

        bottom = anchorpane {
            setLeftAnchor(hbox {
                label(fileProperty.stringBinding {
                    if (it === null) "New file"
                    else it.name
                })
            }, 0.0)

            setRightAnchor(hbox {
                spacing = 12.0

                // Couldn't find an efficient built-in way to get caret line and column in JavaFX or TornadoFX
                val caretPositionProperty = contentTextArea.caretPositionProperty()
                label(stringBinding(caretPositionProperty, contentProperty) {
                    val caretPosition = caretPositionProperty.get()
                    val content = contentProperty.get()
                    val newlineIndexSequence = sequenceOf(0) + content.asSequence().withIndex()
                        .filter { it.value == '\n' }.map { it.index + 1 }
                    val (line, newlineIndex) = newlineIndexSequence.withIndex().last { it.value <= caretPosition }
                    val column = caretPosition - newlineIndex

                    "Ln ${line + 1}, Col ${column + 1}"
                })

                label("LF")
                label("UTF-8")
                label("4 spaces")
                label("12 pt")
            }, 0.0)
        }
    }
}

class RunWithOptionsFragment : Fragment("Run with Options") {
    val programArgs = SimpleStringProperty()
    val compileOnly = SimpleBooleanProperty()
    val generateAst = SimpleBooleanProperty()
    override val root = vbox {
        form {
            field("Program arguments") { textfield(programArgs) }
            checkbox("Compile only", compileOnly)
            checkbox("Generate AST", generateAst)
        }
        button("Run")
    }
}

class OptionsFragment(val optionsVM: OptionsVM = find()) : Fragment("Options"),
    IOptionsProperties by optionsVM.copyToProperties() {
    override val root = vbox {
        form {
            fieldset("Environment settings") {
                field("SDK path") {
                    textfield(sdkPathProperty.bindingWithNullForDefault(""))
                }
            }

            fieldset("Editor settings") {
                field("Line separator") {
                    combobox(lineSeparatorProperty, lineSeparatorsWithNullForDefault)
                }
                field("File encoding") {
                    combobox(fileEncodingProperty, fileEncodingsWithNullForDefault)
                }
                field("Indentation") {
                    val properties = object {
                        val indentationTypeProperty = indentationProperty.bidirectionalBinding(object: Converter<Indentation?, KClass<out Indentation>?> {
                            override fun aToB(a: Indentation?): KClass<out Indentation>? =
                                a?.let { it::class }
                            override fun bToA(b: KClass<out Indentation>?): Indentation? =
                                b?.let { indentationFromTypeAndNumber(it, numberProperty.get()) }
                        })
                        val numberProperty = indentationProperty.bidirectionalBinding(object: Converter<Indentation?, Int?> {
                            override fun aToB(a: Indentation?): Int? =
                                (a as? Indentation.Spaces)?.number

                            override fun bToA(b: Int?): Indentation?
                        })
                    }

                    combobox(properties.indentationTypeProperty, indentationTypesWithNullForDefault)
                    field("Number") {
                        textfield(numberProperty) { enableWhen(indentationTypeProperty.booleanBinding { it == Indentation.Spaces::class }) }
                    }
                }
                field("Font size") {
                    val fontSizeStringProperty = SimpleStringProperty()
                    Bindings.bindBidirectional(fontSizeStringProperty, fontSizeProperty,
                        object : StringConverter<Double?>() {
                            override fun toString(`object`: Double?): String =
                                `object`.orFontSizeDefault().toString()

                            override fun fromString(string: String): Double? =
                                string.toDouble()
                        })
                    textfield(fontSizeStringProperty) { filterInput { it.controlNewText.isDouble() } }
                }
            }
        }
        button("Set to default") {
            action {
                // TODO
            }
        }

        buttonbar {
            button("Save")
            button("Cancel")
        }
    }
}

class AboutCovScriptSimplisticIDEView : View("About $APP_NAME") {
    override val root = TODO()
}

class AboutSdkView : View("About SDK") {
    override val root = TODO()
}