package me.nazarxexe.ui.testing

import me.nazarxexe.ui.click
import me.nazarxexe.ui.component
import me.nazarxexe.ui.drag
import me.nazarxexe.ui.gui
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class BasicGuiCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false

        val gui = gui {
            click { it.isCancelled = true }

            component(0) {
                var count by state(0)
                button {
                    count++
                }
                render {
                    val item = ItemStack(Material.REDSTONE)
                    val mat = item.itemMeta ?: error("Air")
                    mat.setDisplayName("$count")
                    item.itemMeta = mat
                    item
                }
            }
        }

        GuiHandle.openTo(sender, gui)

        return true
    }
}