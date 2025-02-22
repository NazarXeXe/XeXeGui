package me.nazarxexe.ui.testing

import me.nazarxexe.ui.*
import me.nazarxexe.ui.route.gui
import me.nazarxexe.ui.route.subroute
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.annotations.Command

object BasicUICommand {

    @Command("xexeuitest simple")
    fun simpleGui(sender: CommandSender) {
        if (sender !is Player) return
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
                    mat.displayName(Component.text(count))
                    item.itemMeta = mat
                    item
                }
            }
        }

        GuiHandle.openTo(sender, gui)
    }

    @Command("xexeuitest route")
    fun route(sender: CommandSender) {
        if (sender !is Player) return
        me.nazarxexe.ui.route.route(GuiHandle) {
            gui("1") { route ->
                click {
                    it.isCancelled = true
                }
                component(0) {
                    button {
                        route.mov(sender.uniqueId, "2")
                    }
                    render {
                        val item = ItemStack(Material.STONE)
                        val meta = item.itemMeta!!
                        meta.setDisplayName("I'm at 1st gui.")
                        item.itemMeta = meta
                        item
                    }
                }

                component(1) {
                    button {
                        route.mov(sender.uniqueId, "hi/1")
                    }
                    render {
                        ItemStack(Material.REDSTONE).also {
                            it.componentItemMeta.also { c ->
                                c.displayName = minimessage("<red>Go to test/1")
                                it.componentItemMeta = c
                            }
                        }
                    }
                }

            }
            gui("2") { route ->
                click {
                    it.isCancelled = true
                }
                component(1) {
                    button {
                        route.mov(sender.uniqueId, "1")
                    }
                    render {
                        val item = ItemStack(Material.STONE)
                        val meta = item.itemMeta!!
                        meta.setDisplayName("I'm at 2nd gui.")
                        item.itemMeta = meta
                        item
                    }
                }

            }
            subroute("hi", GuiHandle) {
                gui("1") {
                    component(0) {
                        render {
                            ItemStack(Material.DIAMOND).also {
                                it.componentItemMeta.also { c ->
                                    c.displayName = minimessage("<red>Hi")
                                    it.componentItemMeta = c
                                }
                            }
                        }
                    }
                }
            }
            mov(sender.uniqueId, "1")
        }
    }

}