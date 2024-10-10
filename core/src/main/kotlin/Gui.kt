package me.nazarxexe.ui

import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import java.util.UUID

fun interface GuiComposable {
    fun react(e: InventoryEvent)
}

class Gui(val slots: Int) {
    internal val inventory = Bukkit.createInventory(null, slots)

    fun raw(): Array<ItemStack> {
        return inventory.contents
    }
    fun viewers(): List<HumanEntity> {
        return inventory.viewers
    }

    private val uuid = UUID.randomUUID()

    val guiComposable = mutableListOf<GuiComposable>()

    val signalMap = mutableMapOf<Int, () -> Unit>()

    fun <T> state(default: T): GuiState<T> {
        return GuiState(default)
    }

    open fun createSignal(slot: Int, component: GuiComponent): () -> Unit {
        if (slot < 0 || slot >= slots) error("Out of bounds.")
        if (signalMap.containsKey(slot)) error("Slot already in use.")
        val signal = {
            inventory.setItem(slot, component.render())
        }
        signalMap[slot] = signal
        return signal
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Gui) return false
        return other.uuid == this.uuid
    }

}

@Suppress("warnings")
inline fun Gui.compose(e: GuiComposable) {
    guiComposable.add( e )
}

inline fun Gui.open(crossinline impl: (e: InventoryOpenEvent) -> Unit) {
    compose {
        if (it !is InventoryOpenEvent) return@compose
        impl(it)
    }
}

inline fun Gui.close(crossinline impl: (e: InventoryCloseEvent) -> Unit) {
    compose {
        if (it !is InventoryCloseEvent) return@compose
        impl(it)
    }
}

inline fun Gui.click(crossinline impl: (e: InventoryClickEvent) -> Unit) {
    compose {
        if (it !is InventoryClickEvent) return@compose
        impl(it)
    }
}

inline fun gui(slots: Int = InventoryType.CHEST.defaultSize, init: Gui.() -> Unit): Gui {
    val gui = Gui(slots)
    init(gui)
    return gui
}

open class GuiState<T>(private var default: T) {
    open fun value(): T {
        return default
    }
    open fun value(newValue: T) {
        default = newValue
        hooks.forEach { it.signal() }

        return
    }

    val hooks = mutableListOf< ComponentState<*> >()
}

/**
 * Aka read only.
 */
abstract class InternalGuiState<T> {
    abstract fun value(): T

    val hooks = mutableListOf< ComponentState<*> >()
}

interface ClosableState {
    fun close()
}
