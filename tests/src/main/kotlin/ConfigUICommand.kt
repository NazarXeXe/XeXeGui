package me.nazarxexe.ui.testing

import me.nazarxexe.ui.blueprint.BlueprintGui
import me.nazarxexe.ui.blueprint.ConfigSection
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command

class ConfigUICommand(val plugin: XeXeGuiPlugin, val configuredGui: BlueprintGui) {

    @Command("xexeuitest configui")
    fun show(sender: CommandSender) {
        if (sender !is Player) return
        GuiHandle.openTo(sender, configuredGui.make())
    }

    @Command("xexeuitest configui reload")
    fun reload(sender: CommandSender) {
        plugin.reloadConfig()
        configuredGui.visit(ConfigSection(plugin.config, "gui"))
        sender.sendMessage("ok")
    }

}