package shreckye.covscript.simplisticide

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea
import javafx.scene.layout.AnchorPane.setLeftAnchor
import javafx.scene.layout.AnchorPane.setRightAnchor
import tornadofx.*
import java.io.File

class MainApp : App(MainFragment::class)

class MainFragment : Fragment("CovScript simplistic IDE") {
    val fileProperty = SimpleObjectProperty<File?>()
    val originalContentProperty = SimpleStringProperty()
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
        originalContentProperty.set(contentValue)
        contentProperty.set(contentValue)
    }

    fun initNew() = init(null)

    init {
        initNew()
    }

    fun saveWarningIfEdited(continuation: () -> Unit) {
        if (contentProperty.get() != originalContentProperty.get())
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
                    ButtonType.CANCEL -> Unit
                }
            }
        else continuation()
    }

    fun save() {
        val pathValue = fileProperty.get()
        if (pathValue === null) saveAs() else saveAs(pathValue)
    }

    fun saveAs() {
        val file =
            chooseFile("Save As...", FILE_FILTERS, mode = FileChooserMode.Save, owner = currentWindow).getOrNull(0)
        file?.let { saveAs(it) }
    }

    fun saveAs(file: File) {
        file.writeText(contentProperty.get())
        originalContentProperty.set(contentProperty.get())
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
                item("Save", "Ctrl+S") {
                    action { save() }
                }
                item("Save As...", "Ctrl+Shift+S") {
                    action { saveAs() }
                }
                separator()
                item("Exit") {
                    action { close() }
                }
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
                item("Run", "Ctrl+R")
                item("Run Configurations...")
                item("Run Debugger")
                separator()
                item("View Error Info")
                item("Run Installer")
                item("REPL")
                separator()
                item("Build Independent Executable...")
                item("Install Extensions...")
                item("Options...")
            }

            menu("Help") {
                item("About CovScript Simplistic IDE")
                item("SDK Version Info")
                separator()
                item("Visit CovScript Homepage")
                item("View Documentation")
            }
        }

        center = textarea(contentProperty).also { contentTextArea = it }

        bottom = anchorpane {
            setLeftAnchor(hbox {
                label("New file")
                separator(Orientation.VERTICAL)
                label("Encoding: UTF-8")
            }, 0.0)

            setRightAnchor(hbox {
                alignment = Pos.BASELINE_RIGHT
                label("Ln 1, Col 1")
                separator(Orientation.VERTICAL)
                label("Spaces: 4")
            }, 0.0)
        }
    }
}