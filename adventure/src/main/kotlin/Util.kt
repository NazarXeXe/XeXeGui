package me.nazarxexe.ui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun minimessage(parse: String, vararg tagResolver: TagResolver): Component {
    return MiniMessage.miniMessage().deserialize(parse, *tagResolver)
}
internal fun toLegacy(component: Component): String {
    return LegacyComponentSerializer.legacySection().serialize(component)
}
internal fun fromLegacy(component: String): Component {
    return LegacyComponentSerializer.legacySection().deserialize(component)
}