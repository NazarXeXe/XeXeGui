package me.nazarxexe.ui.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.nazarxexe.ui.Gui
import me.nazarxexe.ui.Scheduler

open class AsyncGui(s: Int, val mainScope: CoroutineScope, val asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO)): Gui(s) {
    constructor(s: Int, mainScope: Scheduler, asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO)): this(s, CoroutineScope(SchedulerScope(mainScope)), asyncScope)
}

fun asyncGui(s: Int, mainScope: CoroutineScope, asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AsyncGui {
    return AsyncGui(s, mainScope, asyncScope)
}
fun asyncGui(s: Int, mainScope: Scheduler, asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AsyncGui {
    return AsyncGui(s, mainScope, asyncScope)
}
fun asyncGui(s: Int, mainScope: CoroutineScope, asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO), impl: AsyncGui.() -> Unit): AsyncGui {
    val theGui = AsyncGui(s, mainScope, asyncScope)
    impl(theGui)
    return theGui
}
fun asyncGui(s: Int, mainScope: Scheduler, asyncScope: CoroutineScope = CoroutineScope(Dispatchers.IO), impl: AsyncGui.() -> Unit): AsyncGui {
    val theGui = AsyncGui(s, mainScope, asyncScope)
    impl(theGui)
    return theGui
}
