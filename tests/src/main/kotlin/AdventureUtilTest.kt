package me.nazarxexe.ui.testing

import me.nazarxexe.ui.Scheduler
import me.nazarxexe.ui.component
import me.nazarxexe.ui.gui
import me.nazarxexe.ui.pulse.pulse
import me.nazarxexe.ui.shimmer.shimmer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class AdventureUtilTest(val scheduler: Scheduler) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false
        val gui = gui(InventoryType.CHEST.defaultSize) {
            val shimmer = shimmer(scheduler)
            val pulse = pulse(scheduler)
            component(0) {
                val myShimmer by hook(shimmer)
                render {
                    val item = ItemStack(Material.WHITE_WOOL)
                    val mat = item.itemMeta!!
                    mat.setDisplayName(
                        LegacyComponentSerializer.legacySection().serialize(
                            myShimmer.applyTo("THE SHIMMMMMMMEEEERRR!!!")
                        )
                    )
                    item.itemMeta = mat
                    item
                }
            }
            component(1) {
                val myPulse by hook(pulse)
                render {
                    val item = ItemStack(Material.WHITE_WOOL)
                    val mat = item.itemMeta!!
                    mat.setDisplayName(
                        LegacyComponentSerializer.legacySection().serialize(
                            Component.text("PULSEEEEEE").color(myPulse.color(NamedTextColor.WHITE))
                        )
                    )
                    item.itemMeta = mat
                    item
                }
            }


        }
        GuiHandle.openTo(sender, gui)
        return true
    }
}