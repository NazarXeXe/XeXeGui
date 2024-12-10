package me.nazarxexe.ui.testing

import me.nazarxexe.ui.blueprint.BlueprintGui
import me.nazarxexe.ui.blueprint.ConfigSection
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ConfigGuiTestCommand(val plugin: XeXeGuiPlugin, val configuredGui: BlueprintGui): CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false
        if (args == null) return false

        if (args[0] == "reload") {
            plugin.reloadConfig()
            configuredGui.visit(ConfigSection(plugin.config, "gui"))
            sender.sendMessage("ok")
        }
        if (args[0] == "show") {
            GuiHandle.openTo(sender, configuredGui.make())
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String> {
        return mutableListOf("reload", "show")
    }


}