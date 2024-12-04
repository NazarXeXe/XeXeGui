package me.nazarxexe.ui.pagination

import me.nazarxexe.ui.blueprint.*
import kotlin.properties.ReadOnlyProperty

class BlueprintPagination(
    val name: String,
): SubBlueprint<GuiMakingProcess> {
    private var placeholder = '?'
    private var configured = false
    var pagination: Pagination? = null

    override fun name(): String = name

    override fun configure(section: ConfigSection): BlueprintResult {
        if (!section.isConfigurationSection()) return error("Isn't a section!")
        val cs = section.asConfigurationSection()
        val character = cs.getString("character") ?: return error("Placeholder character is empty!")
        placeholder = character.first()
        configured = true
        return ok()
    }

    override fun configured(): Boolean = configured

    override fun hook(parent: GuiMakingProcess) {
        pagination = parent.gui.pagination(parent.blueprint.mapIndexed { index, c ->
            if (c == placeholder) return@mapIndexed index
            return@mapIndexed null
        }.filterNotNull().toSet())
    }
}

fun BlueprintGuiBuilder.pagination(section: String): ReadOnlyProperty<Any?, Pagination?> {
    val cpag = BlueprintPagination(section)
    addSubBlueprint(cpag)
    return ReadOnlyProperty { _, _ ->
        return@ReadOnlyProperty cpag.pagination
    }
}
