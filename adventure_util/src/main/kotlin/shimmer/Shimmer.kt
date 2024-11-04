package me.nazarxexe.ui.shimmer

import me.nazarxexe.ui.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder


class ShimmerInternalState(
    scheduler: Scheduler,
    val curve: (Float) -> Float = Interp,
    val timePerTick: Float = 0.05f
) : InternalGuiState<ShimmerState>(), ClosableState {

    var state: ShimmerState = ShimmerState(curve(0f))
        set(value) {
            field = value
            hooks.forEach { it.signal() }
        }

    val task = scheduler.runRepeat {
        time += timePerTick
        state = ShimmerState(curve(time))
    }

    var time = 0f
        private set(value) {
            if (value > 1f) {
                field = 0f
                return
            }
            field = value
        }

    override fun value(): ShimmerState {
        return state
    }

    override fun close() {
        task.cancel()
    }


}

data class ShimmerState(private val shift: Float) {

    fun applyTo(
        content: String,
        colors: List<String> = listOf("#fefffc", "#bcbdbb"),
    ): Component {
        val raw = colors.reduce { a, b -> "$a:$b:" }
        return minimessage("<gradient:$raw$shift>$content</gradient>")
    }

    fun applyTo(
        content: ComponentLike,
        colors: List<String> = listOf("#fefffc", "#bcbdbb"),
    ): Component {
        val raw = colors.reduce { a, b -> "$a:$b:" }
        val tag = Placeholder.component("content", content)

        return minimessage("<gradient:$raw$shift><content></gradient>", tag)
    }


}

fun Gui.shimmer(scheduler: Scheduler, timePerTick: Float = 0.05f): ShimmerInternalState {
    return shimmer(scheduler, Interp, timePerTick)
}

fun Gui.shimmer(scheduler: Scheduler, curve: (Float) -> Float, timePerTick: Float = 0.05f): ShimmerInternalState {
    val state = ShimmerInternalState(scheduler, curve, timePerTick)
    close {
        if (viewers().size <= 1) state.close()
    }
    return state
}



