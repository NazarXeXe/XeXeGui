package me.nazarxexe.ui.testing

import com.google.gson.Gson
import me.nazarxexe.ui.async.suspense
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import me.nazarxexe.ui.Scheduler
import me.nazarxexe.ui.click
import me.nazarxexe.ui.componentItemMeta
import me.nazarxexe.ui.gui
import me.nazarxexe.ui.shimmer.shimmer
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class AsyncGuiTestCommand(val scheduler: Scheduler) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false

        GuiHandle.openTo(sender, gui {
            val client = HttpClient(CIO)
            click { it.isCancelled = true }
            suspense(0, scheduler) {
                fallback {
                    val shimmer by shimmer(scheduler)
                    button {
                        it.whoClicked.sendMessage("I'm still loading.")
                    }
                    render {
                        ItemStack(Material.PLAYER_HEAD).also { itm ->
                            itm.componentItemMeta.also {
                                it.displayName = shimmer.applyTo("||||||||||||||||||")
                                it.lore(shimmer.applyTo(
                                    "|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||"
                                ))
                                itm.componentItemMeta = it
                            }
                        }
                    }
                }
                suspendingComponent {
                    delay(2000)
                    val theQuote = Gson().fromJson(client.get("https://programming-quotesapi.vercel.app/api/random")
                        .bodyAsText(), ProgrammerQuote::class.java)

                    button {
                        it.whoClicked.sendMessage("I'm loaded :)")
                    }
                    render {
                        ItemStack(Material.PLAYER_HEAD).also { itm ->
                            itm.itemMeta!!.also {
                                it.setDisplayName(theQuote.author)
                                it.lore = listOf(theQuote.quote)
                                itm.itemMeta = it
                            }
                        }
                    }
                }
            }
        })



        return true
    }
}