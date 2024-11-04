package me.nazarxexe.ui.testing

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import me.nazarxexe.ui.Scheduler
import me.nazarxexe.ui.minimessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandExecutor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class XeXeGuiPlugin : JavaPlugin() {

    fun register(cmd: String, executor: CommandExecutor) {
        val o = getCommand(cmd) ?: error("Command $cmd not found check the plugin.yml.")
        o.setExecutor(executor)
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(GuiHandle, this)
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
    }
}