package shreckye.covscript.simplisticide

import VERSION
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea
import javafx.scene.layout.AnchorPane.setLeftAnchor
import javafx.scene.layout.AnchorPane.setRightAnchor
import javafx.scene.text.FontWeight
import shreckye.covscript.BinDirectory
import shreckye.covscript.simplisticide.tornadofx.currentWindowAlert
import shreckye.covscript.simplisticide.tornadofx.isPositiveDouble
import shreckye.covscript.simplisticide.tornadofx.isPositiveInt
import shreckye.covscript.simplisticide.tornadofx.textfield
import tornadofx.*
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path

class MainApp : App(MainFragment::class)

class MainFragment(val preferencesVM: AppPreferencesVM = find()) : Fragment(APP_NAME),
    IAppPreferenceReadOnlyProperties by preferencesVM {
    val fileProperty = SimpleObjectProperty<File?>()
    val savedContentBytesProperty = SimpleObjectProperty<ByteArray?>()
    val contentProperty = SimpleStringProperty()

    fun getContentBytes() =
        contentProperty.get().replace(
            javafxLineSeparator.toLineSeparatorString(),
            lineSeparatorProperty.get().orDefault().toLineSeparatorString()
        ).toByteArray(fileEncodingProperty.get().orFileEncodingDefault())

    lateinit var contentTextArea: TextArea

    fun init(file: File?) {
        fileProperty.set(file)
        if (file === null)
            initContent(null, "")
        else {
            val fileContentBytes = file.readBytes()
            val fileContentWithEncodingProcessedButLineSeparatorNotProcessed = fileContentBytes
                .toString(fileEncodingProperty.get().orFileEncodingDefault())

            val fileContent = fileContentWithEncodingProcessedButLineSeparatorNotProcessed.replace(
                lineSeparatorProperty.get().orDefault().toLineSeparatorString(),
                javafxLineSeparator.toLineSeparatorString()
            )
            initContent(fileContentBytes, fileContent)

            // Show warnings on line separators if needed
            val distinctSeparators =
                extractDistinctSeparators(fileContentWithEncodingProcessedButLineSeparatorNotProcessed)
            when (distinctSeparators.size) {
                0 -> Unit
                1 -> {
                    val separator = distinctSeparators.single()
                    val preferenceSeparator = lineSeparatorProperty.get().orDefault()
                    if (separator !== preferenceSeparator)
                        currentWindowAlert(
                            Alert.AlertType.WARNING,
                            "Mismatched line separators",
                            "The line separator used in the file is $separator while it's set to $preferenceSeparator for the editor. Its content might not be displayed or processed properly."
                        )
                }
                else -> {
                    currentWindowAlert(
                        Alert.AlertType.WARNING,
                        "Mixed line separators",
                        "The file contains mixed line separators: ${distinctSeparators.joinToString()}. Its content might not be displayed or processed properly."
                    )
                }
            }
        }
    }

    private fun initContent(contentBytes: ByteArray?, content: String) {
        savedContentBytesProperty.set(contentBytes)
        contentProperty.set(content)
    }

    fun initNew() = init(null)

    fun showSaveWarningIfEdited(continuation: () -> Unit, cancelContinuation: () -> Unit) {
        val bytes = getContentBytes()
        if (!(bytes contentEquals (savedContentBytesProperty.get() ?: ByteArray(0))))
            currentWindowAlert(
                Alert.AlertType.WARNING, "The file has been edited. Save it?",
                buttons = arrayOf(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
            ) {
                when (it) {
                    ButtonType.YES -> {
                        save(bytes)
                        continuation()
                    }
                    ButtonType.NO -> continuation()
                    ButtonType.CANCEL -> cancelContinuation()
                }
            }
        else continuation()
    }

    fun showSaveWarningIfEditedWithContinuation(continuation: () -> Unit) =
        showSaveWarningIfEdited(continuation, {})

    fun showSaveWarningIfEditedWithCancelContinuation(cancelContinuation: () -> Unit) =
        showSaveWarningIfEdited({}, cancelContinuation)

    fun save(bytes: ByteArray = getContentBytes()) {
        val file = fileProperty.get()
        if (file === null) saveAs(bytes) else saveAs(bytes, file)
    }

    fun saveAs(bytes: ByteArray = getContentBytes()) {
        val file =
            chooseFile("Save As...", FILE_FILTERS, mode = FileChooserMode.Save, owner = currentWindow).getOrNull(0)
        file?.let {
            fileProperty.set(it)
            saveAs(bytes, it)
        }
    }

    fun saveAs(bytes: ByteArray, file: File) {
        file.writeBytes(bytes)
        savedContentBytesProperty.set(bytes)
    }

    init {
        initNew()
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        // TODO: prevent shutdown with unsaved changes
        currentWindow!!.setOnCloseRequest {
            showSaveWarningIfEditedWithCancelContinuation(it::consume)
        }
    }

    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("New", "Ctrl+N") {
                    action {
                        currentWindowAlert(
                            Alert.AlertType.CONFIRMATION, "Open in a new window?",
                            buttons = arrayOf(ButtonType.YES, ButtonType.NO)
                        ) {
                            when (it) {
                                ButtonType.YES -> find<MainFragment>().openWindow(owner = null)
                                ButtonType.NO -> showSaveWarningIfEditedWithContinuation { initNew() }
                            }
                        }
                    }
                }
                item("Open...", "Ctrl+O") {
                    action {
                        showSaveWarningIfEditedWithContinuation {
                            val file = chooseFile("Open...", FILE_FILTERS, owner = currentWindow).getOrNull(0)
                            file?.let { init(it) }
                        }
                    }
                }
                separator()
                item("Save", "Ctrl+S") { action { save() } }
                item("Save As...", "Ctrl+Shift+S") { action { saveAs() } }
                separator()
                item("Exit") { action { showSaveWarningIfEditedWithContinuation { close() } } }
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
                item("Run", "Ctrl+R") {
                    action {
                        val sdkPath = sdkPathProperty.get()
                        val file = fileProperty.get()
                        if (sdkPath !== null && file !== null)
                            showSaveWarningIfEditedWithContinuation {
                                val csPath = Path.of(sdkPath, BinDirectory.PATH, BinDirectory.cs).toString()
                                runAndPauseWithWindowsCmdWindow(csPath, file.name, directory = file.parentFile)
                            }
                        else
                            currentWindowAlert(
                                Alert.AlertType.ERROR,
                                listOfNotNull(
                                    if (sdkPath === null) "SDK path is not set" else null,
                                    if (file === null) "please save the file first" else null
                                ).joinToString(" and ").capitalize()
                            )
                    }
                }
                item("Run with Options...") { action { find<RunWithOptionsFragment>().openWindow() } }
                item("Run Debugger") { action { TODO() } }
                separator()
                item("Shell") { action { TODO() } }
                item("View Error Info") { action { TODO() } }
                item("Run Installer") { action { TODO() } }
                item("REPL") { action { TODO() } }
                separator()
                item("Build Independent Executable...") { action { TODO() } }
                item("Install Extensions...") { action { TODO() } }
                item("Preferences...") { action { find<PreferencesFragment>().openModal() } }
            }

            menu("Help") {
                item("About $APP_NAME") { action { find<AboutAppView>().openWindow() } }
                item("About CovScript SDK") { action { find<AboutSdkView>().openWindow() } }
                separator()
                item("Visit CovScript Homepage") {
                    action { hostServices.showDocument("http://covscript.org.cn/") }
                }
                item("View Documentation") { action { TODO() } }
            }
        }

        // TextArea seems to only support LF as its line separator
        center = textarea(contentProperty) {
            // Couldn't find an appropriate bind function
            fontSizeProperty.bindByOnChange {
                style { fontSize = it.orFontSizeDefault().px }
            }
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
                spacing = defaultFontSize

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

                label(lineSeparatorProperty.stringBinding {
                    toEnglishStringWithNullForDefault(defaultLineSeparator, it, LineSeparator::name)
                })
                label(fileEncodingProperty.stringBinding {
                    toEnglishStringWithNullForDefault(defaultFileEncoding, it, Charset::name)
                })
                label(indentationProperty.stringBinding {
                    toEnglishStringWithNullForDefault(defaultIndentation, it, Indentation::toEnglishString)
                })
                label(fontSizeProperty.stringBinding {
                    toEnglishStringWithNullForDefault(defaultFontSize, it) { "$it px" }
                })
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
            fieldset {
                field("Program arguments") { textfield(programArgs) }
            }
            checkbox("Compile only", compileOnly)
            checkbox("Generate AST", generateAst)
        }
        buttonbar {
            button("Run") {
                action { TODO() }
            }
        }
    }
}

class PreferencesFragment(val preferencesVM: AppPreferencesVM = find()) : Fragment("Preferences"),
    IEditAppPreferenceProperties by preferencesVM.copyToEditProperties() {
    override val root = vbox {
        form {
            fieldset("Environment settings") {
                field("SDK path") {
                    // TODO (buggy): its content doesn't show
                    textfield(sdkPathProperty.bindingWithNullForDefault("")) {
                        promptText = "not set"
                    }
                }
            }

            fieldset("Editor settings") {
                field("Line separator") {
                    combobox(lineSeparatorProperty, lineSeparatorsWithNullForDefault) {
                        converter =
                            toEnglishStringOnlyConverterWithNullForDefault(defaultLineSeparator, LineSeparator::name)
                    }
                }
                field("File encoding") {
                    combobox(fileEncodingProperty, fileEncodingsWithNullForDefault) {
                        converter = toEnglishStringOnlyConverterWithNullForDefault(defaultFileEncoding, Charset::name)
                    }
                }
                field("Indentation") {
                    combobox(indentationTypeProperty, indentationTypesWithNullForDefault) {
                        converter = toEnglishStringOnlyConverterWithNullForDefault(
                            defaultIndentation.toEnglishString(),
                            IndentationType::toEnglishString
                        )
                    }
                    field("Number") {
                        textfield(indentationNumberProperty) {
                            enableWhen(indentationTypeProperty.booleanBinding { it == Indentation.Spaces::class })
                            filterInput { it.controlNewText.isPositiveInt() }
                        }
                    }
                }
                field("Font size") {
                    textfield(fontSizeProperty) {
                        filterInput { it.controlNewText.isPositiveDouble() }
                        promptText = "default: $defaultFontSize"
                    }
                }
            }

            button("Restore defaults") {
                action(::setAllNullForDefault)
            }

            buttonbar {
                fun apply(): Boolean {
                    val preferences = try {
                        getAll()
                    } catch (e: Exception) {
                        //e.printStackTrace()
                        currentWindowAlert(Alert.AlertType.ERROR, "Please enter valid preference values.")
                        null
                    }

                    return if (preferences !== null) {
                        preferencesVM.setAll(preferences)
                        preferencesVM.commit()
                        true
                    } else false
                }
                button("Save") {
                    action {
                        if (apply())
                            close()
                    }
                }
                button("Cancel") {
                    action { close() }
                }
                button("Apply") {
                    action { apply() }
                }
            }
        }
    }
}

class AboutAppView : View("About $APP_NAME") {
    override val root = vbox {
        spacing = defaultFontSize
        vbox {
            text(APP_NAME) { style { fontWeight = FontWeight.BOLD } }
            text("Version: $VERSION")
        }
        text(LICENSE)
        textflow {
            text("GitHub repository: ")
            hyperlink(URL) { action { hostServices.showDocument(text) } }
        }
    }
}

class AboutSdkView : View("About SDK") {
    override val root = TODO()
}