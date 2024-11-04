package me.nazarxexe.ui

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

open class GuiHandler : Listener {

    val list = mutableListOf<Gui>()

    fun find(inv: Inventory): Gui? {
        for (gui in list) {
            if (gui.inventory == inv) {
                return gui
            }
        }
        return null
    }

    @EventHandler
    open fun open(e: InventoryOpenEvent) {
        val inv = find(e.inventory) ?: return
        for (guiComposable in inv.guiComposable) {
            guiComposable.react(e)
        }
    }

    @EventHandler
    open fun click(e: InventoryClickEvent) {
        val inv = find(e.inventory) ?: return
        for (guiComposable in inv.guiComposable) {
            guiComposable.react(e)
        }
    }

    @EventHandler
    open fun close(e: InventoryCloseEvent) {
        val inv = find(e.inventory) ?: return
        for (guiComposable in inv.guiComposable) {
            guiComposable.react(e)
        }
        if (inv.viewers().size <= 1) {
            list.removeIf { it.inventory == inv.inventory }
        }
    }


    open fun openTo(player: Player, gui: Gui) {
        list.add(gui)
        player.openInventory(gui.inventory)
    }

}