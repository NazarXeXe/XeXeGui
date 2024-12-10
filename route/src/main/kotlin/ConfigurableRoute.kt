package me.nazarxexe.ui.route

import me.nazarxexe.ui.Gui
import me.nazarxexe.ui.GuiHandler
import me.nazarxexe.ui.blueprint.*

class ConfigurableRoute(
    val guiHandler: GuiHandler,
    val name: String,
    val configurationVisitor: List<ConfigurationVisitor>,
    val makingVisitor: List<MakingVisitor<Route>>
): Blueprint<Route>, NamedBlueprint, MakingVisitor<Route> {

    override fun name(): String = name

    override fun visit(section: ConfigSection): BlueprintResult {
        if (!section.isConfigurationSection()) error("Route isn't section!")
        return processSubVisitors(section, configurationVisitor)
    }

    override fun make(): Route {
        val route = route(guiHandler) {
            configurationVisitor.filterIsInstance<Blueprint<Gui>>().forEach {
                if (it is NamedBlueprint) {
                    gui(it.name(), object : Routable<Route> {
                        override fun make(): Gui {
                            return it.make()
                        }
                    })
                }
            }
        }
        makingVisitor.forEach { it.visit(route) }
        return route
    }

    override fun visit(make: Route) {
        make.subroute(name, make())
    }
}

class ConfiguredRouteBuilder(val guiHandler: GuiHandler, val name: String): ConfigurationAccessor, MakingAccessor {
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

    fun build(): ConfigurableRoute {
        return ConfigurableRoute(guiHandler, name, configurationVisitors, makeVisitors.filterIsInstance<MakingVisitor<Route>>())
    }
}


inline fun ConfiguredRouteBuilder.configuredGui(name: String, impl: BlueprintGuiBuilder.() -> Unit) {
    val builder = BlueprintGuiBuilder(name)
    impl(builder)
    val build = builder.build()
    addConfig(build)
}
fun ConfiguredRouteBuilder.configuredGui(gui: BlueprintGui) {
    addConfig(gui)
}

fun ConfiguredRouteBuilder.configuredSubroute(subroute: ConfiguredRouteBuilder) {
    addMake(subroute.build())
}
fun ConfiguredRouteBuilder.configuredSubRoute(name: String, impl: ConfiguredRouteBuilder.() -> Unit) {
    configuredSubroute(ConfiguredRouteBuilder(guiHandler, name).also(impl))
}