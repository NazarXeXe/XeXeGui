package me.nazarxexe.ui

import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ComponentState<T> : ReadWriteProperty<Any?, T> {

    abstract fun get(): T
    abstract fun set(value: T)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return get()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        set(value)
        signal()
    }

    var signal: () -> Unit = {}
        internal set
}

abstract class GuiComponent(
    protected var signal: () -> Unit = {},
    val composable: List<GuiComposable> = listOf(),
    val states: List<ComponentState<*>> = listOf()
) {
    abstract fun render(): ItemStack

    open fun signal() {
        this.signal.invoke()
    }

    open fun changeSignal(signal: () -> Unit) {
        states.forEach {
            it.signal = signal
        }
        this.signal = signal
    }

}

open class GuiComponentBuilder(val slot: Int?) {

    protected var render: () -> ItemStack = { ItemStack(Material.AIR) }
    var stateList = mutableListOf<ComponentState<*>>()
    protected var composableList = mutableListOf<GuiComposable>()

    open fun render(impl: () -> ItemStack) {
        render = impl
    }

    open fun composable(composable: GuiComposable) {
        composableList.add(composable)
    }

    open fun button(button: (e: InventoryClickEvent) -> Unit) {
        composable(Button(slot, button))
    }

    open fun build(): GuiComponent {
        return object : GuiComponent(
            composable = composableList,
            states = stateList
        ) {
            override fun render(): ItemStack {
                return this@GuiComponentBuilder.render()
            }

        }
    }

    fun clone(): GuiComponentBuilder {
        return clone(slot)
    }
    fun clone(at: Int?): GuiComponentBuilder {
        val newBuilder = GuiComponentBuilder(at)
        newBuilder.render = render

        val cl = composableList.toMutableList()

        cl.replaceAll {
            if (it !is Button) return@replaceAll it
            Button(at, it.block)
        }

        newBuilder.composableList = cl
        newBuilder.stateList = stateList
        return newBuilder
    }
}

internal data class Button(var at: Int?, val block: (e: InventoryClickEvent) -> Unit): GuiComposable {
    override fun react(e: InventoryEvent) {
        if (at == null) error("Button slot isn't defined.")
        if (e !is InventoryClickEvent) return
        if (e.slot != at) return
        block(e)
    }

}

@OptIn(ExperimentalContracts::class)
inline fun component(slot: Int? = null, crossinline impl: GuiComponentBuilder.() -> Unit): GuiComponent {
    contract {
        callsInPlace(impl, InvocationKind.EXACTLY_ONCE)
    }
    return componentBuilder(slot, impl).build()
}

@OptIn(ExperimentalContracts::class)
inline fun componentBuilder(slot: Int? = null, crossinline impl: GuiComponentBuilder.() -> Unit): GuiComponentBuilder {
    contract {
        callsInPlace(impl, InvocationKind.EXACTLY_ONCE)
    }
    val builder = GuiComponentBuilder(slot)
    impl(builder)
    return builder
}

inline fun <T> GuiComponentBuilder.tickingState(default: T, scheduler: Scheduler, every: Int = 1, crossinline block: () -> T): ReadWriteProperty<Any?, T> {
    val theState = object : ComponentState<T>(), ClosableState {
        var v: T = default
        val task = scheduler.runRepeat(every) {
            v = block()
            signal()
        }

        override fun close() {
            task.cancel()
        }

        override fun get(): T = v

        override fun set(value: T) {
            v = value
        }
    }
    stateList.add(theState)
    return theState
}

fun <T> GuiComponentBuilder.state(default: T): ReadWriteProperty<Any?, T> {
    val state = object : ComponentState<T>() {
        var v: T = default
        override fun get(): T = v

        override fun set(value: T) {
            v = value
        }

    }
    stateList.add(state)
    return state
}

fun <T> GuiComponentBuilder.hook(state: GuiState<T>): ComponentState<T> {
    val theState = object : ComponentState<T>() {
        override fun get(): T = state.value()

        override fun set(value: T) {
            state.value(value)
        }
    }
    stateList.add(theState)
    state.hooks.add(theState)
    return theState
}

fun <T> GuiComponentBuilder.hook(state: InternalGuiState<T>): ComponentState<T> {
    val theState = object : ComponentState<T>() {
        override fun get(): T = state.value()
        override fun set(value: T) = error("Read only!")
    }
    stateList.add(theState)
    state.hooks.add(theState)
    return theState
}
