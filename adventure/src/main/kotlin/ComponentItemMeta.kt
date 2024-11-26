package me.nazarxexe.ui

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ComponentItemMeta(private val itemMeta: ItemMeta) {

    var displayName: Component = fromLegacy(itemMeta.displayName)
    var lore: List<Component> = (itemMeta.lore ?: emptyList()).map { fromLegacy(it) }.toList()

    fun lore(vararg components: Component) {
        lore = components.toList()
    }

    fun toItemMeta(): ItemMeta {
        val itemMeta = itemMeta.clone()
        itemMeta.setDisplayName(toLegacy(displayName))
        itemMeta.lore = lore.map { toLegacy(it) }.toList()
        return itemMeta
    }

}


var ItemStack.componentItemMeta
    get() = ComponentItemMeta(this.itemMeta ?: error("ItemStack doesn't have ItemMeta."))
    set(value) {
        this.itemMeta = value.toItemMeta()
    }