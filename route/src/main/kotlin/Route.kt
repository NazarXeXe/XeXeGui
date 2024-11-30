package me.nazarxexe.ui.route

import me.nazarxexe.ui.Gui
import me.nazarxexe.ui.GuiHandler
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import java.util.*

open class Route(
    val guiHandler: GuiHandler,
) {
    private val map = mutableMapOf<String, Routable<*>>() // Map literally
    private val subroutes = mutableMapOf<String, Route>()

    open fun gui(path: String, routable: Routable<*>) {
        if (path.contains("/")) error("Gui path shouldn't contain /!")
        map[path] = routable
    }
    open fun subroute(path: String, route: Route) {
        subroutes[path] = route
    }

    open fun mov(player: UUID, to: String) {
        if (!to.contains("/")) {
            val result = map[to] ?: error("Couldn't find any guis for $to.")
            guiHandler.openTo(Bukkit.getPlayer(player) ?: error("Player is offline."), result.make())
            return
        }
        val split = to.split("/")
        val subroute = subroutes[split[0]] ?: error("Subroute ${split[0]} not found.")

        val og = StringBuffer()

        split.toMutableList().also {
            it.removeFirst()
            it.forEachIndexed { index, s ->
                if (index == (it.size-1)) {
                    og.append(s)
                } else {
                    og.append(s).append("/")
                }
            }
        }
        subroute.mov(player, og.toString())
    }
}

inline fun route(handler: GuiHandler, block: Route.() -> Unit): Route {
    val route = Route(
        guiHandler = handler,
    )
    block(route)
    return route
}
inline fun <T: Route> T.subroute(name: String, handler: GuiHandler, block: Route.() -> Unit) {
    val route = Route(
        guiHandler = handler,
    )
    block(route)
    this.subroute(name, route)
}


fun <T: Route> T.gui(path: String, slots: Int = InventoryType.CHEST.defaultSize, builder: Gui.(T) -> Unit) {
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


