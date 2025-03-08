package me.nazarxexe.ui.testing

import me.nazarxexe.ui.Scheduler
import me.nazarxexe.ui.blueprint.*
import me.nazarxexe.ui.click
import me.nazarxexe.ui.componentItemMeta
import me.nazarxexe.ui.minimessage
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager

class XeXeGuiPlugin : JavaPlugin() {

    lateinit var commandManager: LegacyPaperCommandManager<CommandSender>

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

        val result = configuredGui.visit(ConfigSection(config, "gui"))

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

        commandManager = LegacyPaperCommandManager.createNative(
            this,
            ExecutionCoordinator.simpleCoordinator()
        )

        AnnotationParser(commandManager, CommandSender::class.java)
            .parse(
                BasicUICommand,
                AdventureCommand(shedul),
                AsyncUICommand(shedul),
                ConfigUICommand(this, configuredGui),
            )


    }

}