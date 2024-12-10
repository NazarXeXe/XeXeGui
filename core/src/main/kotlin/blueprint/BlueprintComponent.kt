package me.nazarxexe.ui.blueprint

import me.nazarxexe.ui.GuiComponentBuilder
import me.nazarxexe.ui.component

abstract class BlueprintComponent(
    val name: String,
    val makeVisitors: List<MakingVisitor<GuiComponentBuilder>>,
    val configurationVisitors: List<ConfigurationVisitor>,
): MakingVisitor<GuiMakingProcess>, ConfigurationVisitor, Blueprint<GuiComponentBuilder>, NamedBlueprint {

    private var placeholder = '?'

    override fun visit(make: GuiMakingProcess) {
        make.blueprint.mapIndexed { index, s ->
            if (s == placeholder) return@mapIndexed index
            return@mapIndexed null
        }.filterNotNull().forEach {
            val gui = make().clone(it)
            makeVisitors.forEach { s -> s.visit(gui) }
            make.gui.component(it, gui.build())
        }
    }

    override fun name(): String = name

    override fun visit(section: ConfigSection): BlueprintResult {
        if (!section.isConfigurationSection()) return error("Isn't a section!")
        val cs = section.asConfigurationSection()

        val character = cs.getString("character") ?: return error("Placeholder character is empty!")
        placeholder = character.first()
        return processSubVisitors(section, configurationVisitors)
    }

}

class BlueprintComponentBuilder(val name: String): ConfigurationAccessor, MakingAccessor {
    private val configurationVisitors: MutableList<ConfigurationVisitor> = mutableListOf()
    private val makeVisitors: MutableList<MakingVisitor<*>> = mutableListOf()

    private var makes: GuiComponentBuilder.() -> Unit = {}

    fun component(impl: GuiComponentBuilder.() -> Unit) {
        makes = impl
    }

    fun build(): BlueprintComponent {
        return object : BlueprintComponent(name, makeVisitors.filterIsInstance<MakingVisitor<GuiComponentBuilder>>(), configurationVisitors) {
            override fun make(): GuiComponentBuilder {
                val component = GuiComponentBuilder(null)
                makes(component)
                return component
            }
        }
    }

    override fun configurationVisitors(): List<ConfigurationVisitor> = configurationVisitors
    override fun addConfig(hook: ConfigurationVisitor) {
        configurationVisitors.add(hook)
    }

    override fun makingVisitors(): List<MakingVisitor<*>> = makeVisitors
    override fun addMake(hook: MakingVisitor<*>) {
        makeVisitors.add(hook)
    }
}

fun BlueprintGuiBuilder.configuredComponent(name: String, impl: BlueprintComponentBuilder.() -> Unit) {
    val c = configuredComponentBuilder(name, impl).build()
    addConfig(c)
    addMake(c)
}

fun configuredComponentBuilder(name: String, impl: BlueprintComponentBuilder.() -> Unit): BlueprintComponentBuilder {
    return BlueprintComponentBuilder(name).apply(impl)
}