package me.nazarxexe.ui.blueprint


/**
 * @property R Receive
 */
interface SubBlueprint<R> {
    fun name(): String
    fun hook(parent: R)
    fun configure(section: ConfigSection) : BlueprintResult
    fun configured(): Boolean

}