package me.nazarxexe.ui.blueprint

import org.bukkit.configuration.ConfigurationSection

data class ConfigSection(val parent: ConfigurationSection, val child: String) {
    fun isConfigurationSection(): Boolean {
        return parent.isConfigurationSection(child)
    }
    fun asConfigurationSection(): ConfigurationSection {
        return parent.getConfigurationSection(child) ?: error("$parent -> $child is not a configuration section.")
    }

    inline fun <reified T> get(): T {
        if (isConfigurationSection()) error("$parent -> $child is a configuration section not a field.")
        val value = parent.get(child)
        if (value !is T) error("$parent -> $child is not type of ${T::class.simpleName}")
        return value
    }
    inline fun <reified T> isType(): Boolean {
        if (isConfigurationSection()) return false
        return parent.get(child) is T
    }

}