package me.nazarxexe.ui.pulse

import me.nazarxexe.ui.*
import me.nazarxexe.ui.signals.Signal
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.HSVLike
import kotlin.math.sin

class PulseSignal(
    scheduler: Scheduler,
    val curve: (Float) -> Float = { (sin(it)+1)/2 },
    val timePerTick: Float = 0.2f
): Signal<PulseState> {
    override val hooks: MutableList<() -> Unit> = mutableListOf()
    override fun addHook(hook: () -> Unit) {
        hooks.add(hook)
    }

    var state: PulseState = PulseState(curve(0f))
        set(value) {
            field = value
            hooks.forEach { it() }
        }
    var time = 0f
    val task = scheduler.runRepeat {
        time += timePerTick
        state = PulseState(curve(time))
    }

    override fun value(): PulseState {
        return state
    }

    override fun value(value: PulseState) = error("Pulse signal is read only.")
}

class PulseState(val shift: Float) {
    fun color(
        color: TextColor,
    ): TextColor {
        val hsv = color.asHSV()
        val darker = HSVLike.hsvLike(hsv.h(), hsv.s(), 0.6f)
        return TextColor.lerp(shift, color, TextColor.color(darker))
    }

    fun applyTo(
        component: ComponentLike
    ): Component {
        var newComponent = Component.empty()
        val compact = component.asComponent().compact()
        val iter = compact
            .iterator(ComponentIteratorType.BREADTH_FIRST)
        iter.forEach {
            if (it == compact) return@forEach
            val ccolor = it.color() ?: return@forEach
            newComponent = newComponent.append(it.color(color(ccolor))) // No way I cooked this...
        }

        return newComponent
    }

}
fun pulse(scheduler: Scheduler, timePerTick: Float = 0.2f): PulseSignal {
    return pulse(scheduler, { (sin(it)+1)/2 }, timePerTick)
}

fun pulse(scheduler: Scheduler, curve: (Float) -> Float, timePerTick: Float = 0.2f): PulseSignal {
    val state = PulseSignal(scheduler, curve, timePerTick)
    return state
}
