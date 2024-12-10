package me.nazarxexe.ui.blueprint

import me.nazarxexe.ui.Gui
class BlueprintGui(
    val name: String,
    val makeVisitors: List<MakingVisitor<GuiMakingProcess>>,
    val configurationVisitors: List<ConfigurationVisitor>
): Blueprint<Gui>, NamedBlueprint {
    var inventorySize = -1
    var blueprint: List<String> = listOf()

    override fun name(): String {
        return name
    }

    override fun visit(section: ConfigSection): BlueprintResult {
        if (!section.isConfigurationSection()) return error("Isn't a section!")
        val cs = section.asConfigurationSection()
        inventorySize = cs.getInt("inventory_size")
        blueprint = cs.getStringList("blueprint")
        if (blueprint.isEmpty()) return error("Blueprint is empty!")
        val blueprintStream = blueprint.reduce { a, b -> a + b }
        if (blueprintStream.length > inventorySize) return error("Blueprint is too long!")
        return processSubVisitors(section, configurationVisitors)
    }

    override fun make(): Gui {
        val gui = Gui(inventorySize)
        val process = GuiMakingProcess(gui, blueprint.reduce { a, b -> a + b })
        makeVisitors.forEach { it.visit(process) }
        return gui
    }

}

class BlueprintGuiBuilder(val name: String): ConfigurationAccessor, MakingAccessor {
    private val makingVisitors = mutableListOf<MakingVisitor<*>>()
    private val configurationVisitors = mutableListOf<ConfigurationVisitor>()


    fun gui(impl: Gui.() -> Unit) {
        makingVisitors.add(object : MakingVisitor<GuiMakingProcess> {
            override fun visit(make: GuiMakingProcess) {
                impl(make.gui)
            }
        })
    }

    fun build(): BlueprintGui {
        return BlueprintGui(name, makingVisitors.filterIsInstance<MakingVisitor<GuiMakingProcess>>(), configurationVisitors)
    }

    override fun configurationVisitors(): List<ConfigurationVisitor> = configurationVisitors

    override fun addConfig(hook: ConfigurationVisitor) {
        configurationVisitors.add(hook)
    }

    override fun makingVisitors(): List<MakingVisitor<*>> = makingVisitors

    override fun addMake(hook: MakingVisitor<*>) {
        makingVisitors.add(hook)
    }

}

data class GuiMakingProcess(val gui: Gui, val blueprint: String)

inline fun configuredGui(name: String, impl: BlueprintGuiBuilder.() -> Unit): BlueprintGui {
    val builder = BlueprintGuiBuilder(name)
    impl(builder)
    return builder.build()
}
