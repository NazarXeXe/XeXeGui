package me.nazarxexe.ui

import me.nazarxexe.ui.signals.Signal
import me.nazarxexe.ui.signals.signal
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

abstract class GuiComponent(
    protected var signal: () -> Unit = {},
    val composable: List<GuiComposable> = listOf(),
) {
    abstract fun render(): ItemStack

    open fun signal() {
        this.signal.invoke()
    }

    open fun changeSignal(signal: () -> Unit) {
        this.signal = signal
    }

}

open class GuiComponentBuilder(val slot: Int?) {

    protected var render: () -> ItemStack = { ItemStack(Material.AIR) }
    protected var composableList = mutableListOf<GuiComposable>()
    protected val buildPromise = mutableListOf<(GuiComponent) -> Unit>()


    open fun render(impl: () -> ItemStack) {
        render = impl
    }

    fun <T: Signal<R>, R> single(signal: T): T {
        buildPromise.add {
            signal.addHook {
                it.signal()
            }
        }
        return signal
    }
    fun hook(vararg signals: Signal<*>) {
        signals.forEach { signal ->
            buildPromise.add {
                signal.addHook {
                    it.signal()
                }
            }
        }
    }

    open fun ref(): Signal<GuiComponent?> {
        val ref = signal<GuiComponent?>(null)
        buildPromise.add {
            ref.value(it)
        }
        return ref
    }

    open fun composable(composable: GuiComposable) {
        composableList.add(composable)
    }

    open fun button(button: (e: InventoryClickEvent) -> Unit) {
        composable(Button(slot, button))
    }

    open fun build(): GuiComponent {
        val component = object : GuiComponent(
            composable = composableList,
        ) {
            override fun render(): ItemStack {
                return this@GuiComponentBuilder.render()
            }

        }
        buildPromise.forEach { it(component) }
        return component
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
        return newBuilder
    }
}

internal data class Button(var at: Int?, val block: (e: InventoryClickEvent) -> Unit): GuiComposable {
    override fun react(e: InventoryEvent) {
        if (at == null) error("Button slot isn't defined.")
        if (e !is InventoryClickEvent) return
        if (e.rawSlot != at) return
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

inline fun <T> GuiComponentBuilder.state(default: T): Signal<T> {
    val signal = signal(default)
    hook(signal)
    return signal
}
