package me.nazarxexe.ui.async

import kotlinx.coroutines.*
import me.nazarxexe.ui.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Suspense(val slot: Int? = null, val mainScope: Scheduler, val asyncScope: CoroutineScope) {

    private var fallback: GuiComponent = component {  }
    private var suspendingComponent: suspend GuiComponentBuilder.() -> Unit = {}

    fun fallback(fallback: GuiComponentBuilder.() -> Unit) {
        val gcomponent = GuiComponentBuilder(slot)
        fallback(gcomponent)
        this.fallback = gcomponent.build()
    }

    fun suspendingComponent(suspending: suspend GuiComponentBuilder.() -> Unit) {
        suspendingComponent = suspending
    }

    fun make(): GuiComponent {
        return component(slot) {
            val composables = mutableListOf<GuiComposable>()
            var child by single(syncSignal<GuiComponent?>(mainScope, null))

            val ref by ref()
            asyncScope.launch {
                child = fallback
                child?.changeSignal { ref?.signal() }
                composables.addAll(fallback.composable)
                val componentBuilder = GuiComponentBuilder(slot)
                suspendingComponent(componentBuilder)
                composables.clear()
                child?.changeSignal {  }
                componentBuilder.build().also {
                    child = it
                    it.changeSignal { ref?.signal() }
                    composables.addAll(it.composable)
                }
            }
            composable { e ->
                composables.forEach { it.react(e) }
            }
            render {
                child?.render() ?: ItemStack(Material.AIR)
            }
        }
    }

}

inline fun Gui.suspense(slot: Int, mainScope: Scheduler, asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO), impl: Suspense.() -> Unit) {
    val suspense = suspenseBuilder(slot, mainScope, asyncScope, impl)
    val c = suspense.make()
    component(slot, c)
}
inline fun suspenseBuilder(slot: Int, mainScope: Scheduler, asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO), impl: Suspense.() -> Unit): Suspense {
    val suspense = Suspense(slot, mainScope, asyncScope)
    impl(suspense)
    return suspense
}
