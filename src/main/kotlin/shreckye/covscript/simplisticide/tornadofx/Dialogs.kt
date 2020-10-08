package shreckye.covscript.simplisticide.tornadofx

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import tornadofx.UIComponent
import tornadofx.alert

inline fun UIComponent.currentWindowAlert(
    type: Alert.AlertType,
    header: String,
    content: String? = null,
    vararg buttons: ButtonType,
    title: String? = null,
    actionFn: Alert.(ButtonType) -> Unit = {}
): Alert =
    alert(type, header, content, buttons = buttons, currentWindow, title, actionFn)