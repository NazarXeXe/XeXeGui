package me.nazarxexe.ui

import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.ItemStack
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ComponentState<T>: ReadWriteProperty<Any?, T> {
    var signal: () -> Unit = {}
        internal set
}

abstract class GuiComponent(
    private var signal: () -> Unit = {},
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

class GuiComponentBuilder(val slot: Int) {

    private var render: () -> ItemStack = { ItemStack(Material.AIR) }
    private var stateList = mutableListOf<ComponentState<*>>()
    private var composableList = mutableListOf<GuiComposable>()

    fun <T> state(default: T): ReadWriteProperty<Any?, T> {
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
    fun <T> hook(state: GuiState<T>): ComponentState<T> {
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
    fun <T> hook(state: InternalGuiState<T>): ComponentState<T> {
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


    fun render(impl: () -> ItemStack) {
        render = impl
    }

    fun composable(composable: GuiComposable) {
        composableList.add(composable)
    }
    fun button(button: (e: InventoryClickEvent) -> Unit) {
        composable {
            if (it !is InventoryClickEvent) return@composable
            if (it.slot != slot) return@composable
            button(it)
        }
    }

    fun build(): GuiComponent {
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



inline fun Gui.component(slot: Int, crossinline impl: GuiComponentBuilder.() -> Unit ) {
    val builder = GuiComponentBuilder(slot)
    impl(builder)
    val build = builder.build()
    guiComposable.addAll(build.composable)

    component(slot, build)
}

inline fun Gui.component( slot: Int, component: GuiComponent ) {
    component.changeSignal(createSignal(slot, component))
    component.signal()
}