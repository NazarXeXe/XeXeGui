package me.nazarxexe.ui.route

import me.nazarxexe.ui.Gui
import me.nazarxexe.ui.GuiHandler
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import java.util.UUID

class Route(
    val guiHandler: GuiHandler,
    val player: UUID
) {

    val map = mutableMapOf<String, RoutedGui>() // Map literally

    fun gui(path: String, slots: Int = InventoryType.CHEST.defaultSize,  builder: Gui.(Route) -> Unit) {
        map[path] = RoutedGui(slots, this, builder)
    }

    fun mov(to: String) {
        val result = map[to] ?: error("Couldn't find any guis for $to.")
        guiHandler.openTo(Bukkit.getPlayer(player) ?: error("Player is offline."), result.buildGui())
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

class RoutedGui(val slots: Int, val parent: Route, val guiBuilder: Gui.(Route) -> Unit) {
    internal fun buildGui(): Gui {
        val gui = Gui(slots)
        guiBuilder(gui, parent)
        return gui
    }
}


