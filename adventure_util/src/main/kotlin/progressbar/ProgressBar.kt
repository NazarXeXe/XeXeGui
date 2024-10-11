package me.nazarxexe.ui.progressbar

import me.nazarxexe.ui.Gui
import me.nazarxexe.ui.GuiState
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import kotlin.math.ceil

private fun mul(str: String, times: Int): String {
    var result = ""
    for (i in 0 until times) {
        result += str
    }
    return result
}

class ProgressBarState(progress: Float = 0f) : GuiState<Float>(progress) {
    fun make(
        fillingStyle: TextComponent.() -> Component = { this },
        emptyStyle: TextComponent.() -> Component = { this },
        filling: String = "=",
        empty: String = "-",
        size: Int = 10,
    ): Component {
        val fillingCount = ceil(value() * size).toInt()
        val fillingComponent = fillingStyle(Component.text(mul(filling, fillingCount)))
        val emptyComponent = emptyStyle(Component.text(mul(empty, size - fillingCount)))
        return fillingComponent.append(emptyComponent)
    }

}

fun Gui.progressBar(): ProgressBarState {
    return ProgressBarState()
}

fun Gui.progressBar(default: Float): ProgressBarState {
    return ProgressBarState(default)
}

