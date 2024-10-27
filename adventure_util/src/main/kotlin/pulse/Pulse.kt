package me.nazarxexe.ui.pulse

import me.nazarxexe.ui.*
import net.kyori.adventure.text.format.TextColor
import kotlin.math.sin

class PulseInternalState(
    scheduler: Scheduler,
    val curve: (Float) -> Float = { (sin(it)+1)/2 },
    val timePerTick: Float = 0.2f
): InternalGuiState<PulseState>(), ClosableState {

    var state: PulseState = PulseState(curve(0f))
        set(value) {
            field = value
            hooks.forEach { it.signal() }
        }
    var time = 0f
    val task = scheduler.runRepeat {
        time += timePerTick
        state = PulseState(curve(time))
    }

    override fun value(): PulseState {
        return state
    }

    override fun close() {
        task.cancel()
    }
}

class PulseState(val shift: Float) {
    fun color(
        color: TextColor,
    ): TextColor {
        val darker = TextColor.color(color.red() * 3/4, color.green() * 3/4, color.blue() * 3/4)
        return TextColor.lerp(shift, color, darker)
    }
}
fun Gui.pulse(scheduler: Scheduler, timePerTick: Float = 0.2f): PulseInternalState {
    return pulse(scheduler, { (sin(it)+1)/2 }, timePerTick)
}

fun Gui.pulse(scheduler: Scheduler, curve: (Float) -> Float, timePerTick: Float = 0.2f): PulseInternalState {
    val state = PulseInternalState(scheduler, curve, timePerTick)
    close {
        if (viewers().size <= 1) state.close()
    }
    return state
}