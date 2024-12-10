package me.nazarxexe.ui.pagination

import me.nazarxexe.ui.blueprint.*
import kotlin.properties.ReadOnlyProperty

class BlueprintPagination(
    val name: String,
    val configurationVisitors: List<ConfigurationVisitor>,
    val makingVisitors: List<MakingVisitor<Pagination>>
): MakingVisitor<GuiMakingProcess>, ConfigurationVisitor, NamedBlueprint {
    private var placeholder = '?'
    var pagination: Pagination? = null

    override fun name(): String = name

    override fun visit(section: ConfigSection): BlueprintResult {
        if (!section.isConfigurationSection()) return error("Isn't a section!")
        val cs = section.asConfigurationSection()
        val character = cs.getString("character") ?: return error("Placeholder character is empty!")
        val errs = configurationVisitors.map {
            val subSection: ConfigSection = if (it is NamedBlueprint)
                ConfigSection(cs, it.name())
            else
                section
            it.visit(subSection)
        }.filterIsInstance<BlueprintError>()
        if (errs.isNotEmpty()) return errors(errs)
        placeholder = character.first()
        return ok()
    }

    override fun visit(make: GuiMakingProcess) {
        val pg = make.gui.pagination(make.blueprint.mapIndexed { index, c ->
            if (c == placeholder) return@mapIndexed index
            return@mapIndexed null
        }.filterNotNull().toSet())
        pagination = pg
        makingVisitors.forEach { it.visit(pg) }
    }
}

class BlueprintPaginationBuilder(val name: String): ConfigurationAccessor, MakingAccessor {
    private val configurationVisitors: MutableList<ConfigurationVisitor> = mutableListOf()
    private val makeVisitors: MutableList<MakingVisitor<*>> = mutableListOf()

    override fun configurationVisitors(): List<ConfigurationVisitor> = configurationVisitors
    override fun addConfig(hook: ConfigurationVisitor) {
        configurationVisitors.add(hook)
    }

    override fun makingVisitors(): List<MakingVisitor<*>> = makeVisitors
    override fun addMake(hook: MakingVisitor<*>) {
        makeVisitors.add(hook)
    }

    fun build(): BlueprintPagination {
        return BlueprintPagination(name, configurationVisitors, makeVisitors.filterIsInstance<MakingVisitor<Pagination>>())
    }
}


fun BlueprintGuiBuilder.pagination(section: String): ReadOnlyProperty<Any?, Pagination?> {
    val cpag = BlueprintPaginationBuilder(section).build()
    addConfig(cpag)
    addMake(cpag)
    return ReadOnlyProperty { _, _ ->
        return@ReadOnlyProperty cpag.pagination
    }
}
fun BlueprintGuiBuilder.pagination(section: String, impl: BlueprintPaginationBuilder.() -> Unit) {
    val cpag = BlueprintPaginationBuilder(section).apply(impl).build()
    addConfig(cpag)
    addMake(cpag)
}
