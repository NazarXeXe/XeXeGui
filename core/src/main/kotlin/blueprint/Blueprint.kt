package me.nazarxexe.ui.blueprint

import kotlin.properties.ReadOnlyProperty

interface Blueprint<T> {
    fun make(): T
}

interface ConfigurationAccessor {
    fun configurationVisitors(): List<ConfigurationVisitor>
    fun addConfig(hook: ConfigurationVisitor)
}
interface MakingAccessor {
    fun makingVisitors(): List<MakingVisitor<*>>
    fun addMake(hook: MakingVisitor<*>)
}

fun ConfigurationAccessor.visit(impl: (ConfigSection) -> BlueprintResult) {
    addConfig(object : ConfigurationVisitor {
        override fun visit(section: ConfigSection): BlueprintResult {
            return impl(section)
        }
    })
}
fun <T> MakingAccessor.visit(impl: (T) -> Unit) {
    addMake(object : MakingVisitor<T> {
        override fun visit(make: T) {
            impl(make)
        }
    })
}


interface ConfigurationVisitor {
    fun visit(section: ConfigSection): BlueprintResult
}
interface MakingVisitor<T> {
    fun visit(make: T)
}


inline fun <reified T> ConfigurationAccessor.configuredField(name: String): ReadOnlyProperty<Any?, T?> {
    var value: T? = null
    addConfig(object : ConfigurationVisitor, NamedBlueprint {
        override fun name(): String {
            return name
        }

        override fun visit(section: ConfigSection): BlueprintResult {
            if (!section.isType<T>()) return error("${section.parent} -> ${section.child} is not a type of ${T::class.simpleName}!")
            value = section.get<T>()
            return ok()
        }
    })
    return ReadOnlyProperty { _, _ ->
        value
    }
}

fun processSubVisitors(section: ConfigSection, subVisitors: List<ConfigurationVisitor>): BlueprintResult {
    val errs = subVisitors.map {
        val subSection: ConfigSection = if (it is NamedBlueprint)
            ConfigSection(section.asConfigurationSection(), it.name())
        else
            section
        it.visit(subSection)
    }.filterIsInstance<BlueprintError>()
    if (errs.isNotEmpty()) return BlueprintErrors(section.child, errs)
    return ok()
}