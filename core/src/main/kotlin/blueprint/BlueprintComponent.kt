package me.nazarxexe.ui.blueprint

import me.nazarxexe.ui.GuiComponentBuilder
import me.nazarxexe.ui.component
import org.bukkit.configuration.ConfigurationSection
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class BlueprintComponent(
    val name: String,
    val subBlueprints: List<SubBlueprint<GuiComponentBuilder>>,
): SubBlueprint<GuiMakingProcess> {

    private var placeholder = '?'
    private var configured = false

    abstract fun make(at: Int): GuiComponentBuilder

    override fun hook(parent: GuiMakingProcess) {
        parent.blueprint.mapIndexed { index, s ->
            if (s == placeholder) return@mapIndexed index
            return@mapIndexed null
        }.filterNotNull().forEach {
            val gui = make(it)
            subBlueprints.forEach { s -> s.hook(gui) }
            parent.gui.component(it, gui.build())
        }
    }

    override fun name(): String {
        return name
    }

    override fun configured(): Boolean = configured

    override fun configure(section: ConfigSection): BlueprintResult {
        if (!section.isConfigurationSection()) return error("Isn't a section!")
        val cs = section.asConfigurationSection()
        
        val character = cs.getString("character") ?: return error("Placeholder character is empty!")
        placeholder = character.first()
        val errs = subBlueprints.map {
            val sub = ConfigSection(cs, it.name())
            it.configure(sub)
        }.filterIsInstance<BlueprintError>()
        if (errs.isNotEmpty()) return errors(errs)
        configured = true
        return ok()
    }

}

class BlueprintComponentBuilder(val name: String) {
    private val subBlueprints: MutableList<SubBlueprint<GuiComponentBuilder>> = mutableListOf()
    private var makes: GuiComponentBuilder.() -> Unit = {}

    fun addSubBlueprint(subBlueprint: SubBlueprint<GuiComponentBuilder>) {
        subBlueprints.add(subBlueprint)

    }

    fun component(impl: GuiComponentBuilder.() -> Unit) {
        makes = impl
    }

    fun build(): BlueprintComponent {
        return object : BlueprintComponent(name, subBlueprints) {
            override fun make(at: Int): GuiComponentBuilder {
                val component = GuiComponentBuilder(at)
                makes(component)
                return component
            }
        }
    }
}

fun BlueprintGuiBuilder.configuredComponent(name: String, impl: BlueprintComponentBuilder.() -> Unit) {
    addSubBlueprint(configuredComponentBuilder(name, impl).build())
}

fun configuredComponentBuilder(name: String, impl: BlueprintComponentBuilder.() -> Unit): BlueprintComponentBuilder {
    return BlueprintComponentBuilder(name).apply(impl)
}

inline fun <reified T> BlueprintComponentBuilder.configuredField(name: String): ReadOnlyProperty<Any?, T?> {
    var value: T? = null
    addSubBlueprint(object : SubBlueprint<GuiComponentBuilder> {
        override fun name(): String {
            return name
        }
        override fun configure(section: ConfigSection): BlueprintResult {
            if (!section.isType<T>()) error("${section.parent} -> ${section.child} is not a type of ${T::class.simpleName}!")
            value = section.get<T>()
            return ok()
        }
        override fun configured(): Boolean = true
        override fun hook(parent: GuiComponentBuilder) = Unit
    })
    return ReadOnlyProperty { _, _ ->
        value
    }
}
inline fun <reified T> BlueprintGuiBuilder.configuredField(name: String): ReadOnlyProperty<Any?, T?> {
    var value: T? = null
    addSubBlueprint(object : SubBlueprint<GuiMakingProcess> {
        override fun name(): String {
            return name
        }
        override fun configure(section: ConfigSection): BlueprintResult {
            if (!section.isType<T>()) error("${section.parent} -> ${section.child} is not a type of ${T::class.simpleName}!")
            value = section.get<T>()
            return ok()
        }
        override fun configured(): Boolean = true
        override fun hook(parent: GuiMakingProcess) = Unit
    })
    return ReadOnlyProperty { _, _ ->
        value
    }
}