package me.nazarxexe.ui.testing

import me.nazarxexe.ui.click
import me.nazarxexe.ui.component
import me.nazarxexe.ui.gui
import me.nazarxexe.ui.pagination.Page
import me.nazarxexe.ui.pagination.dynPagination
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PaginationTestCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false

        val theGui = gui {
            click {
                it.isCancelled = true
            }
            val pagination = dynPagination(10..16, cache = true) { thePage ->
                val page = Page(this)

                for (i in 0..6) {
                    page.set(i + 10, component {
                        render {
                            ItemStack(Material.DIRT).also { itm ->
                                itm.itemMeta!!.also {
                                    it.setDisplayName("${i + (6 * thePage)}")
                                    itm.itemMeta = it
                                }
                            }
                        }
                    })
                }

                page
            }
            pagination.page = 0 // Update
            component(21) {
                button {
                    if (pagination.page > 0)
                        pagination.page--
                }
                render {
                    ItemStack(Material.ARROW)
                        .also { itm ->
                            itm.itemMeta!!.also {
                                it.setDisplayName("Prev")
                                itm.itemMeta = it
                            }
                        }
                }
            }
            component(23) {
                button {
                    pagination.page++
                }
                render {
                    ItemStack(Material.ARROW)
                        .also { itm ->
                            itm.itemMeta!!.also {
                                it.setDisplayName("Next")
                                itm.itemMeta = it
                            }
                        }
                }
            }
        }
        GuiHandle.openTo(sender, theGui)



        return true
    }
}