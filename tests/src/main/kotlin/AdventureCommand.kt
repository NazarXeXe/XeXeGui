package me.nazarxexe.ui.testing

import me.nazarxexe.ui.*
import me.nazarxexe.ui.progressbar.progressBar
import me.nazarxexe.ui.pulse.pulse
import me.nazarxexe.ui.shimmer.shimmer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.annotations.Command
import kotlin.math.floor

class AdventureCommand(private val scheduler: Scheduler) {


    @Command("xexeuitest adventure")
    fun show(sender: CommandSender) {
        if (sender !is Player) return
        val gui = gui(InventoryType.CHEST.defaultSize) {
            val shimmer = shimmer(scheduler)
            val pulse = pulse(scheduler)
            val pgbr = progressBar()
            click { it.isCancelled = true }
            component(0) {
                val myShimmer by hook(shimmer)
                render {
                    val item = ItemStack(Material.WHITE_WOOL)
                    val mat = item.componentItemMeta
                    mat.displayName = myShimmer.applyTo("THE SHIMMMMMMMEEEERRR!!!")
                    item.componentItemMeta = mat
                    item
                }
            }
            component(1) {
                val myPulse by hook(pulse)
                render {
                    val item = ItemStack(Material.WHITE_WOOL)
                    val mat = item.componentItemMeta
                    mat.displayName = Component.text("PULSEEEEEE").color(myPulse.color(NamedTextColor.WHITE))
                    mat.lore(myPulse.applyTo(minimessage("<rainbow>Colorful pulsed.")).asComponent())
                    item.componentItemMeta = mat
                    item
                }
            }
            component(2) {
                var progressBar by hook(pgbr)
                val task = scheduler.runRepeat(20) {
                    if (progressBar >= 1) {
                        progressBar = 0f
                        return@runRepeat
                    }
                    progressBar += 0.1f
                }
                render {
                    ItemStack(Material.DIRT).also { itm ->
                        itm.componentItemMeta.also {
                            it.displayName = pgbr.make().append(minimessage(" <white>${floor(progressBar*100)}%</white>"))
                            itm.componentItemMeta = it
                        }
                    }
                }
                compose {
                    if (it !is InventoryCloseEvent) return@compose
                    task.cancel()
                }
            }



        }
        GuiHandle.openTo(sender, gui)
    }

}