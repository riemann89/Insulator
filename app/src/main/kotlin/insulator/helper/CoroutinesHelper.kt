package insulator.helper

import javafx.application.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch

fun <T : Any> T.runOnFXThread(f: T.() -> Unit) = Platform.runLater { this.apply(f) }

fun <T : Any> T.dispatch(block: suspend T.() -> Unit) = GlobalScope.launch(Dispatchers.JavaFx) { block() }
