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
                hook(shimmer)
                render {
                    val item = ItemStack(Material.WHITE_WOOL)
                    val mat = item.componentItemMeta
                    mat.displayName = shimmer.value().applyTo("THE SHIMMMMMMMEEEERRR!!!")
                    item.componentItemMeta = mat
                    item
                }
            }
            component(1) {
                hook(pulse)
                hook(shimmer)
                render {
                    val item = ItemStack(Material.WHITE_WOOL)
                    val mat = item.componentItemMeta
                    mat.displayName = Component.text("PULSEEEEEE").color(pulse.value().color(NamedTextColor.WHITE))
                    mat.lore(
                        pulse.value().applyTo(minimessage("<rainbow>Colorful pulsed.")),
                        pulse.value().applyTo(
                            shimmer.value().applyTo("Shiiiimmmmeeerrr!")
                        )
                    )
                    item.componentItemMeta = mat
                    item
                }
            }
            component(2) {
                var progressBar by single(pgbr)
                val shim by single(shimmer)
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
                            it.displayName = pgbr
                                .make(fillingStyle = {
                                    shim.applyTo(this)
                                })
                                .append(minimessage(" <white>${floor(progressBar*100)}%</white>"))
                            itm.componentItemMeta = it
                        }
                    }
                }
                close {
                    task.cancel()
                }
            }


        }
        GuiHandle.openTo(sender, gui)
    }

}