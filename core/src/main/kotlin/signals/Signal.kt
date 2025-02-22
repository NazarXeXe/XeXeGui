package me.nazarxexe.ui.signals

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Signal<T>: ReadWriteProperty<Any?, T> {
    val hooks: List<() -> Unit>
    fun addHook(hook: () -> Unit)

    fun value(): T
    fun value(value: T)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        value(value)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value()
}

fun <T> signal(default: T): Signal<T> {
    return object : Signal<T> {
        var value = default
        override val hooks: MutableList<() -> Unit> = mutableListOf()
        override fun addHook(hook: () -> Unit) {
            hooks.add(hook)
        }

        override fun value(): T {
            return value
        }

        override fun value(value: T) {
            this.value = value
            hooks.forEach { it() }
        }

    }
}