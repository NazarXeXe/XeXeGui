package me.nazarxexe.ui

import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ComponentState<T> : ReadWriteProperty<Any?, T> {
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
    protected var stateList = mutableListOf<ComponentState<*>>()
    protected var composableList = mutableListOf<GuiComposable>()

    open fun <T> state(default: T): ReadWriteProperty<Any?, T> {
        val state = object : ComponentState<T>() {
            var v: T = default
            override fun getValue(thisRef: Any?, property: KProperty<*>): T = v

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                v = value
                signal()
            }
        }
        stateList.add(state)
        return state
    }

    open fun <T> hook(state: GuiState<T>): ComponentState<T> {
        val theState = object : ComponentState<T>() {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T = state.value()

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                state.value(value)
            }
        }
        stateList.add(theState)
        state.hooks.add(theState)
        return theState
    }

    open fun <T> hook(state: InternalGuiState<T>): ComponentState<T> {
        val theState = object : ComponentState<T>() {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T = state.value()

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                error("Hook $property is read only!")
            }
        }
        stateList.add(theState)
        state.hooks.add(theState)
        return theState
    }


    open fun render(impl: () -> ItemStack) {
        render = impl
    }

    open fun composable(composable: GuiComposable) {
        composableList.add(composable)
    }

    open fun button(button: (e: InventoryClickEvent) -> Unit) {
        if (slot == null) error("Button slot isn't defined.")
        composable {
            if (it !is InventoryClickEvent) return@composable
            if (it.slot != slot) return@composable
            button(it)
        }
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
}

inline fun component(slot: Int? = null, crossinline impl: GuiComponentBuilder.() -> Unit): GuiComponent {
    val builder = GuiComponentBuilder(slot)
    impl(builder)
    return builder.build()
}
