package me.nazarxexe.ui.testing

import me.nazarxexe.ui.Scheduler
import me.nazarxexe.ui.blueprint.*
import me.nazarxexe.ui.click
import me.nazarxexe.ui.componentItemMeta
import me.nazarxexe.ui.minimessage
import org.bukkit.Material
import org.bukkit.command.CommandExecutor
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class XeXeGuiPlugin : JavaPlugin() {

    fun register(cmd: String, executor: CommandExecutor) {
        val o = getCommand(cmd) ?: error("Command $cmd not found check the plugin.yml.")
        o.setExecutor(executor)
    }

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(GuiHandle, this)

        val configuredGui = configuredGui("gui") {
            val canTake by configuredField<Boolean>("canTake")
            gui {
                click { it.isCancelled = !(canTake ?: false) }
            }
            configuredComponent("myComponent") {
                val header by configuredField<String>("header")
                component {
                    render {
                        ItemStack(Material.DIAMOND).also {
                            it.componentItemMeta.also { m ->
                                m.displayName = minimessage(header ?: "Unknown")
                                it.componentItemMeta = m
                            }
                        }
                    }
                }
            }
        }

        val result = configuredGui.configure(ConfigSection(config, "gui"))

        /**
         * I'll give it free pass for now.
         * If error occurs, notify the user that gui is misconfigured and refuse to open the gui
         */
        if (result is BlueprintErrors) {
            logger.warning(result.message)
            result.errors.forEach { logger.warning(it.message) }
        }
        if (result is BlueprintError) {
            logger.warning(result.message)
        }


        val shedul = object : Scheduler {

            override fun run(runnable: () -> Unit): BukkitTask {
                return server.scheduler.runTask(this@XeXeGuiPlugin, runnable)
            }

            override fun runRepeat(repeat: Int, runnable: () -> Unit): BukkitTask {
                return server.scheduler.runTaskTimer(this@XeXeGuiPlugin, runnable, 0, repeat.toLong())
            }
        }
        register("xexegui_test_adv", AdventureUtilTest(shedul))
        register("xexegui_test_basic", BasicGuiCommand())
        register("xexegui_test_progressbar", ProgressBarGuiCommand(shedul))
        register("xexegui_test_route", RouteCommandTest(GuiHandle))
        register("xexegui_test_pagination", PaginationTestCommand())
        register("xexegui_test_suspense", AsyncGuiTestCommand(shedul))
        register("xexegui_test_placeholderapi", PlaceholderAPITestCommand(shedul))
        register("xexegui_test_config", ConfigGuiTestCommand(configuredGui))
    }
}