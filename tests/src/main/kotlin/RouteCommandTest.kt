package me.nazarxexe.ui.testing

import me.nazarxexe.ui.GuiHandler
import me.nazarxexe.ui.click
import me.nazarxexe.ui.component
import me.nazarxexe.ui.route.route
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class RouteCommandTest(val guiHandler: GuiHandler): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false

        route(guiHandler, sender.uniqueId) {
            gui("1") { route ->
                click {
                    it.isCancelled = true
                }
                component(0) {
                    button {
                        route.mov("2")
                    }
                    render {
                        val item = ItemStack(Material.STONE)
                        val meta = item.itemMeta!!
                        meta.setDisplayName("I'm at 1st gui.")
                        item.itemMeta = meta
                        item
                    }
                }
            }
            gui("2") { route ->
                click {
                    it.isCancelled = true
                }
                component(1) {
                    button {
                        route.mov("1")
                    }
                    render {
                        val item = ItemStack(Material.STONE)
                        val meta = item.itemMeta!!
                        meta.setDisplayName("I'm at 2st gui.")
                        item.itemMeta = meta
                        item
                    }
                }
            }
            mov("1")
        }

        return true
    }
}