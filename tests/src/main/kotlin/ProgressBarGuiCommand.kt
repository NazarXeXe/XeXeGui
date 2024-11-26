package me.nazarxexe.ui.testing

import me.nazarxexe.ui.*
import me.nazarxexe.ui.progressbar.progressBar
import me.nazarxexe.ui.shimmer.shimmer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class ProgressBarGuiCommand(val scheduler: Scheduler) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false

        val theGui = gui(InventoryType.CHEST.defaultSize) {
            val progressBar = progressBar()
            val shimmer = shimmer(scheduler, timePerTick = 0.1f)
            component(0) {
                var progress by hook(progressBar)
                val theShimmer by hook(shimmer)

                click {
                    it.isCancelled = true
                }
                button {
                    if (progress >= 1f) {
                        progress = 0f
                        return@button
                    }
                    progress += 0.1f
                }
                render {
                    val item = ItemStack(Material.STONE)
                    val meta = item.componentItemMeta

                    meta.displayName = progressBar.make(
                        size = 30,
                        fillingStyle = {
                            theShimmer.applyTo(this)
                        }
                    )
                    item.componentItemMeta = meta
                    item
                }
            }
        }
        GuiHandle.openTo(sender, theGui)
        return true
    }
}