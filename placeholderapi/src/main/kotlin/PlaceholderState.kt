package me.nazarxexe.ui.placeholderapi

import me.clip.placeholderapi.PlaceholderAPI
import me.nazarxexe.ui.*
import org.bukkit.OfflinePlayer
import org.bukkit.event.inventory.InventoryCloseEvent
import kotlin.properties.ReadOnlyProperty

class InternalGuiPlaceholderAPIState<T>(
    scheduler: Scheduler,
    updateFrequency: Int,
    val toParse: String,
    val player: OfflinePlayer?,
    val customize: (String) -> T
): InternalGuiState<T>(), ClosableState {

    val task = scheduler.runRepeat(updateFrequency) {
        this@InternalGuiPlaceholderAPIState.hooks.forEach { it.signal() }
    }

    override fun value(): T {
        return customize(PlaceholderAPI.setPlaceholders(player, toParse))
    }

    override fun close() {
        task.cancel()
    }
}

class ComponentPlaceholderAPIState<T>(
    scheduler: Scheduler,
    updateFrequency: Int,
    val toParse: String,
    val player: OfflinePlayer?,
    val customize: (String) -> T
): ComponentState<T>(), ClosableState {
    val task = scheduler.runRepeat(updateFrequency) {
        this@ComponentPlaceholderAPIState.signal()
    }
    override fun close() {
        task.cancel()
    }
    override fun get(): T = customize(PlaceholderAPI.setPlaceholders(player, toParse))
    override fun set(value: T) = error("Read only!")
}

fun <T> GuiComponentBuilder.placeholderState(
    scheduler: Scheduler,
    toParse: String,
    player: OfflinePlayer? = null,
    updateFrequency: Int = 10,
    customize: (String) -> T
): ReadOnlyProperty<Any?, T> {
    val state = ComponentPlaceholderAPIState(scheduler, updateFrequency, toParse, player, customize)
    composable {
        if (it !is InventoryCloseEvent) return@composable
        state.close()
    }
    return state
}
fun GuiComponentBuilder.placeholderState(
    scheduler: Scheduler,
    toParse: String,
    player: OfflinePlayer? = null,
    updateFrequency: Int = 10,
): ReadOnlyProperty<Any?, String> {
    return placeholderState(scheduler, toParse, player, updateFrequency) { it }
}

fun <T> Gui.guiPlaceholderState(
    scheduler: Scheduler,
    toParse: String,
    player: OfflinePlayer? = null,
    updateFrequency: Int = 10,
    customize: (String) -> T
): InternalGuiState<T> {
    val state = InternalGuiPlaceholderAPIState(scheduler, updateFrequency, toParse, player, customize)
    close {
        if (it.viewers.size <= 1) state.close()
    }
    return state
}

fun Gui.guiPlaceholderState(
    scheduler: Scheduler,
    toParse: String,
    player: OfflinePlayer? = null,
    updateFrequency: Int = 10,
): InternalGuiState<String> {
    return guiPlaceholderState(scheduler, toParse, player, updateFrequency) { it }
}

