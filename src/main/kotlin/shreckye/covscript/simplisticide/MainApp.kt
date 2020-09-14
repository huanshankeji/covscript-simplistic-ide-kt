package shreckye.covscript.simplisticide

import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.AnchorPane.setLeftAnchor
import javafx.scene.layout.AnchorPane.setRightAnchor
import tornadofx.*

class MainApp : App(MainView::class)

class MainView : View("CovScript simplistic IDE") {
    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("New", "Ctrl+N")
                item("Open...", "Ctrl+O")
                separator()
                item("Save", "Ctrl+S")
                item("Save As...", "Ctrl+Shift+S")
                separator()
                item("Exit")
            }

            menu("Edit") {
                item("Undo", "Ctrl+Z")
                separator()
                item("Cut", "Ctrl+X")
                item("Copy", "Ctrl+C")
                item("Paste", "Ctrl+V")
                item("Select All", "Ctrl+A")
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

        center = textarea()

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