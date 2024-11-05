package me.nazarxexe.ui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

fun minimessage(parse: String, vararg tagResolver: TagResolver): Component {
    return MiniMessage.miniMessage().deserialize(parse, *tagResolver)
}
