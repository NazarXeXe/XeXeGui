package me.nazarxexe.ui.blueprint

interface Blueprint<T> {
    fun name(): String
    fun configure(section: ConfigSection) : BlueprintResult
    fun make(): T
}