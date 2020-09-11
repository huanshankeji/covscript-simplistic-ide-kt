package shreckye.covscript.simplisticide

import tornadofx.*

class MainApp : App(MainView::class)

class MainView : View() {
    override val root = vbox {
        label("Covscript simplistic IDE")
    }
}