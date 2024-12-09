package me.nazarxexe.ui.route

import me.nazarxexe.ui.Gui
import me.nazarxexe.ui.GuiHandler
import me.nazarxexe.ui.blueprint.*
import kotlin.properties.ReadOnlyProperty

class ConfigurableRoute(
    val guiHandler: GuiHandler,
    val name: String,
    val subGuiBlueprints: List<Blueprint<Gui?>>): Blueprint<Route> {
    override fun name(): String {
        return name
    }

    override fun configure(section: ConfigSection): BlueprintResult {
        if (!section.isConfigurationSection()) error("Route isn't section!")
        val errs = subGuiBlueprints.map { it.configure(ConfigSection(section.asConfigurationSection(), it.name())) }.filterIsInstance<BlueprintError>()
        if (errs.isNotEmpty()) return errors(errs)
        return ok()
    }

    override fun make(): Route {
        val route = route(guiHandler) {
            subGuiBlueprints.forEach {
                it.make() ?: return@route
                gui(it.name(), object : Routable<Route> {
                    override fun make(): Gui {
                        return it.make()!!
                    }
                })
            }
        }
        return route
    }
}

class ConfiguredRouteBuilder(val guiHandler: GuiHandler, val name: String) {
    val subBlueprints = mutableListOf<Blueprint<Gui?>>()

    fun build(): ConfigurableRoute {
        return ConfigurableRoute(guiHandler, name, subBlueprints)
    }
}

inline fun <reified T> ConfiguredRouteBuilder.configuredField(name: String): ReadOnlyProperty<Any?, T?> {
    var o: T? = null
    subBlueprints.add(object : Blueprint<Gui?> {
        override fun name(): String {
            return name
        }
        override fun configure(section: ConfigSection): BlueprintResult {
            if (!section.isType<T>()) return error("Section ${section.parent} -> ${section.child} is not a type of ${T::class.simpleName}!")
            o = section.get<T>()
            return ok()
        }
        override fun make(): Gui? = null
    })
    return ReadOnlyProperty { _, _ -> o }
}

inline fun ConfiguredRouteBuilder.configuredGui(name: String, impl: BlueprintGuiBuilder.() -> Unit) {
    subBlueprints.add(me.nazarxexe.ui.blueprint.configuredGui(name, impl))
}
fun ConfiguredRouteBuilder.configuredGui(gui: BlueprintGui) {
    subBlueprints.add(gui)
}
