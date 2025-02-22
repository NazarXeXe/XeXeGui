package me.nazarxexe.ui.shimmer

import me.nazarxexe.ui.*
import me.nazarxexe.ui.signals.Signal
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder


class ShimmerSignal(
    scheduler: Scheduler,
    val curve: (Float) -> Float = Interp,
    val timePerTick: Float = 0.05f
) : Signal<ShimmerState> {
    override val hooks: MutableList<() -> Unit> = mutableListOf()
    override fun addHook(hook: () -> Unit) {
        hooks.add(hook)
    }


    var state: ShimmerState = ShimmerState(curve(0f))
        set(value) {
            field = value
            hooks.forEach { it() }
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

    override fun value(value: ShimmerState) = error("Shimmer signal is read only.")

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

fun shimmer(scheduler: Scheduler, timePerTick: Float = 0.05f): ShimmerSignal {
    return shimmer(scheduler, Interp, timePerTick)
}

fun shimmer(scheduler: Scheduler, curve: (Float) -> Float, timePerTick: Float = 0.05f): ShimmerSignal {
    val state = ShimmerSignal(scheduler, curve, timePerTick)
    return state
}



