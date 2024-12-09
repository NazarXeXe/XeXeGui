package me.nazarxexe.ui.blueprint

import me.nazarxexe.ui.Gui
import java.util.UUID

class BlueprintGui(
    val name: String,
    val blueprints: List<SubBlueprint<GuiMakingProcess>>
): Blueprint<Gui?> {
    var inventorySize = -1
    var blueprint: List<String> = listOf()

    override fun name(): String {
        return name
    }

    override fun configure(section: ConfigSection) : BlueprintResult {
        if (!section.isConfigurationSection()) return error("Isn't a section!")
        val cs = section.asConfigurationSection()
        inventorySize = cs.getInt("inventory_size")
        blueprint = cs.getStringList("blueprint")
        if (blueprint.isEmpty()) return error("Blueprint is empty!")
        val blueprintStream = blueprint.reduce { a, b -> a + b }
        if (blueprintStream.length > inventorySize) return error("Blueprint is too long!")
        val errors = blueprints.map {
            val subSection = ConfigSection(cs, it.name())
            it.configure(subSection)
        }.filterIsInstance<BlueprintError>()
        if (errors.isNotEmpty()) {
            return errors(errors)
        }
        return ok()
    }

    override fun make(): Gui {
        val gui = Gui(inventorySize)
        val process = GuiMakingProcess(gui, blueprint.reduce { a, b -> a + b })
        blueprints.filter { it.configured() }.forEach { it.hook(process) }
        return gui
    }

}

class BlueprintGuiBuilder(val name: String) {
    private val blueprints: MutableList<SubBlueprint<GuiMakingProcess>> = mutableListOf()

    fun gui(impl: Gui.() -> Unit) {
        blueprints.add(object : SubBlueprint<GuiMakingProcess> {
            override fun name(): String {
                return UUID.randomUUID().toString()
            }

            override fun configure(section: ConfigSection): BlueprintResult {
                return ok()
            }

            override fun configured(): Boolean = true

            override fun hook(parent: GuiMakingProcess) {
                impl(parent.gui)
            }
        })
    }

    fun addSubBlueprint(blueprint: SubBlueprint<GuiMakingProcess>) {
        blueprints.add(blueprint)
    }

    fun build(): BlueprintGui {
        return BlueprintGui(name, blueprints)
    }
}

data class GuiMakingProcess(val gui: Gui, val blueprint: String)

inline fun configuredGui(name: String, impl: BlueprintGuiBuilder.() -> Unit): BlueprintGui {
    val builder = BlueprintGuiBuilder(name)
    impl(builder)
    return builder.build()
}
