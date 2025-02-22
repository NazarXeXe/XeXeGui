package me.nazarxexe.ui.async

import me.nazarxexe.ui.Scheduler
import me.nazarxexe.ui.signals.Signal

fun <T> syncSignal(scheduler: Scheduler, default: T): Signal<T> {
    return object : Signal<T> {
        override val hooks: MutableList<() -> Unit> = mutableListOf()
        var value = default
        override fun addHook(hook: () -> Unit) {
            scheduler.run { hooks.add(hook) }
        }

        override fun value(): T = value

        override fun value(value: T) {
            this.value = value
            scheduler.run {
                hooks.forEach { it() }
            }
        }

    }
}