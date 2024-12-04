package me.nazarxexe.ui.testing

import me.nazarxexe.ui.blueprint.BlueprintGui
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ConfigGuiTestCommand(val configuredGui: BlueprintGui): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false
        GuiHandle.openTo(sender, configuredGui.make())
        return true
    }
}