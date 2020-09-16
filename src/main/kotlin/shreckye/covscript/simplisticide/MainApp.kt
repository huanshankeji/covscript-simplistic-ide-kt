package shreckye.covscript.simplisticide

import javafx.beans.property.*
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea
import javafx.scene.control.skin.TextAreaSkin
import javafx.scene.layout.AnchorPane.setLeftAnchor
import javafx.scene.layout.AnchorPane.setRightAnchor
import tornadofx.*
import java.io.File

class MainApp : App(MainFragment::class) {

}

class MainFragment : Fragment(APP_NAME) {
    val fileProperty = SimpleObjectProperty<File?>()
    val savedContentProperty = SimpleStringProperty()
    val contentProperty = SimpleStringProperty()

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
    fun saveWarningIfEdited(continuation: () -> Unit) = saveWarningIfEdited(continuation, {})
    fun saveWarningIfEdited(continuation: () -> Unit, cancelContinuation: () -> Unit) {
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
            saveWarningIfEdited({}, it::consume)
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
                item("Options...")
            }

            menu("Help") {
                item("About $APP_NAME")
                item("SDK Version Info")
                separator()
                item("Visit CovScript Homepage")
                item("View Documentation")
            }
        }

        center = textarea(contentProperty).also { contentTextArea = it }

        bottom = anchorpane {
            setLeftAnchor(hbox {
                label(fileProperty.stringBinding {
                    if (it === null) "New file"
                    else it.name
                })
            }, 0.0)

            setRightAnchor(hbox {
                alignment = Pos.BASELINE_RIGHT
                label("Ln 1, Col 1")
                val caretPositionProperty = contentTextArea.anchor

                @Suppress("UNCHECKED_CAST")
                val skinProperty = contentTextArea.skinProperty() as ObjectProperty<TextAreaSkin>
                label(stringBinding(caretPositionProperty, skinProperty) {
                    val caretPosition = caretPositionProperty.get()
                    val skin = skinProperty.get()
                    skin
                })
                separator(Orientation.VERTICAL)
                label("LF")
                separator(Orientation.VERTICAL)
                label("UTF-8")
                separator(Orientation.VERTICAL)
                label("4 spaces")
                separator(Orientation.VERTICAL)
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

class OptionsView : View("Options") {
    val sdkPath = SimpleStringProperty()
    val fontSize = SimpleIntegerProperty()
    override val root = vbox {
        form {
            field("SDK path") { textfield(sdkPath) }

            fieldset("Editor settings") {

            }
        }
        button("Set to default")

        buttonbar {
            button("Save")
            button("Cancel")
        }
    }
}

class OptionsVM : ViewModel() {
    val sdkPath = SimpleStringProperty()

    val lineSeperator = SimpleObjectProperty<LineSeparator>()
    val encoding = SimpleStringProperty()
    val indentation = SimpleObjectProperty<Indentation>()

    init {
        preferences(NODE_NAME) {
            sdkPath.set(get(SDK_PATH_KEY, null))
            lineSeperator.set(get(LINE_SEPARATOR_KEY, SYSTEM_LINE_SEPARATOR_STRING).let(::))
            encoding.set(get(ENCODING_KEY, ENCODING_DEFAULT_VALUE))
        }
    }
}

class AboutCovScriptSimplisticIDEView : View("About $APP_NAME") {
    override val root = TODO()
}

class AboutSdkView : View("About SDK") {
    override val root = TODO()
}