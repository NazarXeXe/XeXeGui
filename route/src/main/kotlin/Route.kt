package me.nazarxexe.ui.route

import me.nazarxexe.ui.Gui
import me.nazarxexe.ui.GuiHandler
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import java.util.*

open class Route(
    val guiHandler: GuiHandler,
    val player: UUID
) {
    val map = mutableMapOf<String, Routable<*>>() // Map literally
    open fun gui(path: String, routable: Routable<*>) {
        map[path] = routable
    }
    open fun mov(to: String) {
        val result = map[to] ?: error("Couldn't find any guis for $to.")
        guiHandler.openTo(Bukkit.getPlayer(player) ?: error("Player is offline."), result.make())
    }
}

inline fun route(handler: GuiHandler, player: UUID, block: Route.() -> Unit): Route {
    val route = Route(
        guiHandler = handler,
        player = player
    )
    block(route)
    return route
}

fun <T: Route> T.gui(path: String, slots: Int = org.bukkit.event.inventory.InventoryType.CHEST.defaultSize, builder: Gui.(T) -> Unit) {
    gui(path, RoutedGui(slots, this, builder))
}

interface Routable<T: Route> {
    fun make(): Gui
}

class RoutedGui<T: Route>(val slots: Int, val context: T, val guiBuilder: Gui.(T) -> Unit): Routable<T> {
    override fun make(): Gui {
        val gui = Gui(slots)
        guiBuilder(gui, context)
        return gui
    }
}


