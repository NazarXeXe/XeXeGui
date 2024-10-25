import kotlinx.coroutines.*
import me.nazarxexe.ui.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.coroutines.CoroutineContext

class Suspense(val slot: Int? = null, val mainScope: CoroutineScope, val asyncScope: CoroutineScope) {

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
            var child by state<GuiComponent?>(null)
            var dummyState by state(true)
            mainScope.launch {
                child = fallback
                child?.changeSignal { dummyState = !dummyState }
                composables.addAll(fallback.composable)
                val componentBuilder = GuiComponentBuilder(slot)
                asyncScope.launch {
                    suspendingComponent(componentBuilder)
                }.join()
                composables.clear()
                child?.changeSignal {  }
                componentBuilder.build().also {
                    child = it
                    it.changeSignal { dummyState = !dummyState }
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




inline fun AsyncGui.suspense(slot: Int, impl: Suspense.() -> Unit) {
    val suspense = Suspense(slot, this.mainScope, this.asyncScope)
    impl(suspense)
    val c = suspense.make()
    component(slot, c)
    guiComposable.addAll(c.composable)
}
inline fun Gui.suspense(slot: Int, mainScope: CoroutineScope, asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO), impl: Suspense.() -> Unit) {
    val suspense = Suspense(slot, mainScope, asyncScope)
    impl(suspense)
    val c = suspense.make()
    component(slot, c)
    guiComposable.addAll(c.composable)
}
inline fun Gui.suspense(slot: Int, mainScope: Scheduler, asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO), impl: Suspense.() -> Unit) {
    val suspense = Suspense(slot, CoroutineScope(object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            mainScope.run { block.run() }
        }
    }), asyncScope)
    impl(suspense)
    val c = suspense.make()
    component(slot, c)
    guiComposable.addAll(c.composable)
}