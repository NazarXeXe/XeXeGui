package me.nazarxexe.ui.blueprint


interface BlueprintResult
open class BlueprintError(val from: String, val message: String): BlueprintResult
class BlueprintErrors(from: String, val errors: List<BlueprintError>): BlueprintError(from,"Error(s) occur while configuring sub blueprints!")

fun ok(): BlueprintSuccess {
    return BlueprintSuccess
}
fun NamedBlueprint.errors(list: List<BlueprintError>): BlueprintErrors {
    return BlueprintErrors(this.name(),list)
}
fun NamedBlueprint.error(message: String): BlueprintError {
    return BlueprintError(this.name(), message)
}

object BlueprintSuccess: BlueprintResult



