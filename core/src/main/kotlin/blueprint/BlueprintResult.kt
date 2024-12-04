package me.nazarxexe.ui.blueprint


interface BlueprintResult
open class BlueprintError(val from: String, val message: String): BlueprintResult
class BlueprintErrors(from: String, val errors: List<BlueprintError>): BlueprintError(from,"Error(s) occur while configuring sub blueprints!")

fun Blueprint<*>.ok(): BlueprintSuccess {
    return BlueprintSuccess
}
fun Blueprint<*>.errors(list: List<BlueprintError>): BlueprintErrors {
    return BlueprintErrors(this.name(),list)
}
fun Blueprint<*>.error(message: String): BlueprintError {
    return BlueprintError(this.name(), message)
}

fun SubBlueprint<*>.ok(): BlueprintSuccess {
    return BlueprintSuccess
}
fun SubBlueprint<*>.errors(list: List<BlueprintError>): BlueprintErrors {
    return BlueprintErrors(this.name(),list)
}
fun SubBlueprint<*>.error(message: String): BlueprintError {
    return BlueprintError(this.name(), message)
}

object BlueprintSuccess: BlueprintResult



