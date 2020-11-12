package shreckye.covscript.simplisticide

import VERSION
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.layout.AnchorPane.setLeftAnchor
import javafx.scene.layout.AnchorPane.setRightAnchor
import javafx.scene.text.FontWeight
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import shreckye.covscript.*
import shreckye.covscript.CovScriptSdkDirectory.BinDirectory
import shreckye.covscript.CovScriptSdkDirectory.DOCS_DIRECTORY
import shreckye.covscript.CovScriptSdkDirectory.IMPORTS_DIRECTORY
import shreckye.covscript.simplisticide.tornadofx.currentWindowAlert
import shreckye.covscript.simplisticide.tornadofx.isPositiveDouble
import shreckye.covscript.simplisticide.tornadofx.isPositiveInt
import shreckye.covscript.simplisticide.tornadofx.textfield
import tornadofx.*
import java.io.File
import java.io.Reader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class MainApp : App(MainFragment::class)

class MainFragment(val preferencesVM: AppPreferencesVM = find()) : Fragment(APP_NAME),
    IAppPreferenceReadOnlyProperties by preferencesVM {
    val fileProperty = SimpleObjectProperty<File?>()
    val savedContentBytesProperty = SimpleObjectProperty<ByteArray?>()
    //val contentProperty = SimpleStringProperty()

    fun getContentBytes() =
        /*contentProperty.get()*/
        contentCodeArea.text.replace(
            javafxLineSeparator.toLineSeparatorString(),
            lineSeparatorProperty.get().orDefault().toLineSeparatorString()
        ).toByteArray(fileEncodingProperty.get().orFileEncodingDefault())

    lateinit var contentCodeArea: CodeArea

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
        //contentProperty.set(content)
        contentCodeArea.replaceText(content)
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
                        if (save(bytes))
                            continuation()
                        else
                            cancelContinuation()
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

    fun save(bytes: ByteArray = getContentBytes()): Boolean {
        val file = fileProperty.get()
        return if (file === null)
            saveAs(bytes)
        else {
            saveAs(bytes, file)
            true
        }
    }

    fun saveAs(bytes: ByteArray = getContentBytes()): Boolean {
        val oldFile = fileProperty.get()
        val file =
            chooseFile(
                "Save As...", SAVE_FILE_FILTERS, oldFile?.parentFile, FileChooserMode.Save, currentWindow
            ) { initialFileName = oldFile?.name }.getOrNull(0)
        return file?.let {
            fileProperty.set(it)
            saveAs(bytes, it)
            true
        } ?: false
    }

    fun saveAs(bytes: ByteArray, file: File) {
        file.writeBytes(bytes)
        savedContentBytesProperty.set(bytes)
    }

    fun checkSdkPathAndFileAndThenRun(block: (sdkPath: String, file: File) -> Unit) {
        val sdkPath = sdkPathProperty.get()
        val file = fileProperty.get()
        if (sdkPath !== null && file !== null)
            showSaveWarningIfEditedWithContinuation { block(sdkPath, file) }
        else
            currentWindowAlert(
                Alert.AlertType.ERROR,
                listOfNotNull(
                    if (sdkPath === null) "SDK path is not set" else null,
                    if (file === null) "please save the file first" else null
                ).joinToString(" and ").capitalize()
            )
    }

    private fun getCsPath(sdkPath: String) =
        Path.of(sdkPath, BinDirectory.NAME, BinDirectory.cs).toString()

    fun checkSdkPathAndFileAndThenRunWithCs(block: (csPath: String, file: File) -> Unit) =
        checkSdkPathAndFileAndThenRun { sdkPath, file -> block(getCsPath(sdkPath), file) }

    fun getDumpAstText(csPath: String, file: File): String {
        val process = startProcess(csPath, "--compile-only", "--dump-ast", "--no-optimize", file.path)
        return process.inputStream.bufferedReader().use(Reader::readText)
    }

    fun checkSdkPathAndThenRun(block: (sdkPath: String) -> Unit) {
        val sdkPath = sdkPathProperty.get()
        if (sdkPath !== null)
            block(sdkPath)
        else
            currentWindowAlert(Alert.AlertType.ERROR, "SDK path is not set")
    }

    fun checkSdkPathAndThenRunWithCs(block: (csPath: String) -> Unit) =
        checkSdkPathAndThenRun { sdkPath -> block(getCsPath(sdkPath)) }

    override fun onBeforeShow() {
        super.onBeforeShow()
        // TODO: prevent shutdown with unsaved changes
        currentWindow!!.setOnCloseRequest {
            showSaveWarningIfEditedWithCancelContinuation(it::consume)
        }
    }

    override val root = borderpane {
        setPrefSize(800.0, 600.0)
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
                            val file = chooseFile(
                                "Open...", SAVE_FILE_FILTERS, fileProperty.get()?.parentFile, owner = currentWindow
                            ).getOrNull(0)
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
                // Lambdas can't be replaced with references here for the var `contentCodeArea`
                item("Undo", "Ctrl+Z") { action { contentCodeArea.undo() } }
                item("Redo", "Ctrl+Shift+Z") { action { contentCodeArea.redo() } }
                separator()
                item("Cut", "Ctrl+X") { action { contentCodeArea.cut() } }
                item("Copy", "Ctrl+C") { action { contentCodeArea.copy() } }
                item("Paste", "Ctrl+V") { action { contentCodeArea.paste() } }
                item("Select All", "Ctrl+A") { action { contentCodeArea.selectAll() } }
            }

            menu("Tools") {
                item("Run", "Ctrl+R") {
                    action {
                        checkSdkPathAndFileAndThenRunWithCs { csPath, file ->
                            currentOSTerminalActions.runProcessAndPauseWithTerminal(
                                csPath,
                                file.name,
                                directory = file.parentFile
                            )
                        }
                    }
                }
                item("Run with Options...") { action { RunWithOptionsFragment(this@MainFragment).openWindow() } }
                item("Dump AST to...") {
                    action {
                        checkSdkPathAndFileAndThenRunWithCs { csPath, file ->
                            val dumpAstText = getDumpAstText(csPath, file)
                            val dumpAstFile = chooseFile(
                                "Dump AST to...",
                                AST_FILE_FILTERS, file.parentFile, FileChooserMode.Save, currentWindow
                            ) {
                                initialFileName = file.name.run {
                                    val lastDotIndex = lastIndexOf(".")
                                    val baseName = if (lastDotIndex != -1) substring(0, lastDotIndex) else this
                                    "$baseName.csa"
                                }
                            }
                                .getOrNull(0)
                            dumpAstFile?.run { writeText(dumpAstText) }
                        }
                    }
                }
                item("Run Debugger") {
                    action {
                        checkSdkPathAndFileAndThenRun { sdkPath, file ->
                            val csDbgPath = Path.of(sdkPath, BinDirectory.NAME, BinDirectory.csDbg).toString()
                            currentOSTerminalActions.runProcessWithTerminal(
                                csDbgPath, file.name, directory = file.parentFile
                            )
                        }
                    }
                }
                separator()
                item("Terminal") {
                    action {
                        fileProperty.get()?.let { currentOSTerminalActions.openTerminal(it.parentFile) }
                            ?: currentOSTerminalActions.openTerminal()
                    }
                }
                item("View Error Info") {
                    action {
                        checkSdkPathAndThenRun { sdkPath ->
                            val csLogFile = Path.of(sdkPath, BinDirectory.NAME, BinDirectory.csLog).toFile()
                            if (csLogFile.exists()) {
                                val errorText = csLogFile.readText()
                                currentWindowAlert(Alert.AlertType.INFORMATION, "Error Info", errorText)
                            } else
                                currentWindowAlert(Alert.AlertType.INFORMATION, "No Error Info")
                        }
                    }
                }
                item("Run Installer") {
                    action {
                        checkSdkPathAndThenRun { sdkPath ->
                            val csInstFile = Path.of(sdkPath, BinDirectory.NAME, BinDirectory.csInst).toFile()
                            desktopOpen(csInstFile)
                        }
                    }
                }
                item("REPL") {
                    action {
                        checkSdkPathAndThenRunWithCs(currentOSTerminalActions::runProcessWithTerminal)
                    }
                }
                separator()
                //item("Build Independent Executable...")
                item("Add SDK Extensions...") {
                    action {
                        checkSdkPathAndThenRun { sdkPath ->
                            val file = chooseFile("Install SDK Extensions...", EXTENSION_FILE_FILTERS).getOrNull(0)
                            file?.let {
                                try {
                                    Files.copy(file.toPath(), Path.of(sdkPath, IMPORTS_DIRECTORY, file.name))
                                } catch (e: FileAlreadyExistsException) {
                                    currentWindowAlert(
                                        Alert.AlertType.ERROR, "An extension file with the same name already exists"
                                    )
                                }
                            }
                        }
                    }
                }
                item("Preferences...") { action { find<PreferencesFragment>().openModal() } }
            }

            menu("Help") {
                item("About $APP_NAME") { action { find<AboutAppView>().openWindow() } }
                item("About CovScript") {
                    action {
                        checkSdkPathAndThenRun {
                            find<AboutCovscriptAndSdkView>().openWindow()
                        }
                    }
                }
                separator()
                item("Visit CovScript Homepage") {
                    action { hostServices.showDocument("http://covscript.org.cn/") }
                }
                item("View Documentation") {
                    action {
                        checkSdkPathAndThenRun { sdkPath ->
                            desktopOpen(File(sdkPath, DOCS_DIRECTORY))
                        }
                    }
                }
            }
        }

        // TextArea seems to only support LF as its line separator
        center = VirtualizedScrollPane(CodeArea().apply {
            // Couldn't find an appropriate bind function
            fontSizeProperty.bindByOnChange {
                style { fontSize = it.orFontSizeDefault().px }
            }
            setOnKeyTyped {
                if (it.character == "\t") {
                    val currentParagraph = currentParagraph
                    val caretColumn = caretColumn
                    replaceText(
                        currentParagraph, caretColumn - 1, currentParagraph, caretColumn,
                        indentationProperty.get().orDefault().text
                    )
                }
            }

            paragraphGraphicFactory = LineNumberFactory.get(this)
            stylesheets.add(MainFragment::class.java.getResource("covscript-highlight.css").toExternalForm())
            paragraphs.addModificationObserver {
                for (parIndex in it.from until it.to) {
                    val syntaxSegmentss = findCovscriptSyntaxSegmentss(getText(parIndex))

                    fun highlightSegments(ranges: SyntaxSegmentRanges, cssClass: String) {
                        for (range in ranges)
                            setStyle(
                                parIndex, range.start, range.endInclusive + 1,
                                Collections.singleton(cssClass)
                            )
                    }

                    with(syntaxSegmentss) {
                        // Follows Visual Studio Code Light+ Color Theme C++ color scheme
                        highlightSegments(comments, "comment")
                        highlightSegments(stringLiterals, "string-literal")
                        highlightSegments(numberLiterals, "number-literal")
                        highlightSegments(preprocessingStatements, "preprocessing-statement")
                        highlightSegments(keywords, "keyword")
                        highlightSegments(symbols, "symbol")
                        highlightSegments(functions, "function")
                        highlightSegments(variables, "variable")
                    }
                }
            }
        }
            .also { contentCodeArea = it })

        bottom = anchorpane {
            setLeftAnchor(hbox {
                label(fileProperty.stringBinding {
                    if (it === null) "New file"
                    else it.name
                })
            }, 0.0)

            setRightAnchor(hbox {
                spacing = defaultFontSize

                val lineProperty = contentCodeArea.currentParagraphProperty()
                val columnProperty = contentCodeArea.caretColumnProperty()
                label(stringBinding(lineProperty, columnProperty) {
                    "Ln ${lineProperty.value + 1}, Col ${columnProperty.value + 1}"
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


    init {
        initNew()
    }
}

class RunWithOptionsFragment(mainFragment: MainFragment) : Fragment("Run with Options") {
    val programArgs = SimpleStringProperty("")
    val compileOnly = SimpleBooleanProperty()
    val dumpAst = SimpleBooleanProperty()

    override val root = vbox {
        form {
            fieldset {
                field("Program arguments") { textfield(programArgs) }
            }
            checkbox("Compile only", compileOnly)
            checkbox("Dump AST", dumpAst)
        }
        buttonbar {
            button("Run") {
                action {
                    mainFragment.checkSdkPathAndFileAndThenRunWithCs { csPath, file ->
                        currentOSTerminalActions.runProcessAndPauseWithTerminal(
                            *listOf(
                                listOf(csPath),
                                programArgs.get().split(' '),
                                listOfNotNull(
                                    if (compileOnly.get()) "--compile-only" else null,
                                    if (dumpAst.get()) "--dump-ast" else null
                                ),
                                listOf(file.name)
                            ).flatten().toTypedArray(), directory = file.parentFile
                        )
                    }
                }
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

class AboutCovscriptAndSdkView : View("About CovScript") {
    override val root = vbox {
        spacing = defaultFontSize
        text(COVSCRIPT_FULL_NAME)
        imageview(COVSCRIPT_ICON_WIDE_URL) {
            isPreserveRatio = true
            fitWidthProperty().bind(widthProperty())
        }
        textflow {
            text("Homepage: ")
            hyperlink(COVSCRIPT_HOMEPATE_URL) { action { hostServices.showDocument(text) } }
        }
        textflow {
            text("GitHub repository: ")
            hyperlink(COVSCRIPT_GITHUB_URL) { action { hostServices.showDocument(text) } }
        }
    }
}