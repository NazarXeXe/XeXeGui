package me.nazarxexe.ui.testing

import me.nazarxexe.ui.*
import me.nazarxexe.ui.placeholderapi.guiPlaceholderState
import me.nazarxexe.ui.placeholderapi.placeholderState
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PlaceholderAPITestCommand(val scheduler: Scheduler): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false


        GuiHandle.openTo(sender, gui {
            fun phs(toParse: String, player: OfflinePlayer?): InternalGuiState<String> {
                return guiPlaceholderState(scheduler, toParse, player)
            }

            click { it.isCancelled = true }

            val guiPlayerPing = phs("%player_ping%", sender)
            component(0) {
                val playerPing by hook(guiPlayerPing)
                val experience by placeholderState(scheduler, "%player_exp_to_level%", sender)
                render {
                    ItemStack(Material.DIAMOND).also { itm ->
                        itm.componentItemMeta.also {
                            it.displayName = minimessage("<gray>Your ping: <green>$playerPing")
                            it.lore(minimessage("<gray>Your experience: $experience"))
                            itm.componentItemMeta = it
                        }
                    }
                }
            }

        })


        return true
    }
}