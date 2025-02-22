package me.nazarxexe.ui.progressbar

import me.nazarxexe.ui.signals.Signal
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import kotlin.math.floor

private fun mul(str: String, times: Int): String {
    var result = ""
    for (i in 0 until times) {
        result += str
    }
    return result
}

class ProgressBarSignal(var progress: Float = 0f) : Signal<Float> {
    override val hooks: MutableList<() -> Unit> = mutableListOf()
    override fun addHook(hook: () -> Unit) {
        hooks.add(hook)
    }

    override fun value(): Float = progress

    override fun value(value: Float) {
        progress = value
    }

    fun make(
        fillingStyle: TextComponent.() -> Component = { this },
        emptyStyle: TextComponent.() -> Component = { this },
        filling: String = "=",
        empty: String = "-",
        size: Int = 10,
    ): Component {
        val fillingCount = floor(   value() * size).toInt()
        val fillingComponent = fillingStyle(Component.text(mul(filling, fillingCount)))
        val emptyComponent = emptyStyle(Component.text(mul(empty, size - fillingCount)))
        return fillingComponent.append(emptyComponent)
    }

}

fun progressBar(): ProgressBarSignal {
    return ProgressBarSignal()
}

fun progressBar(default: Float): ProgressBarSignal {
    return ProgressBarSignal(default)
}

