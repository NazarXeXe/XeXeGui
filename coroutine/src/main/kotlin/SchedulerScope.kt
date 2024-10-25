package me.nazarxexe.ui.async

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import me.nazarxexe.ui.Scheduler
import kotlin.coroutines.CoroutineContext

class SchedulerScope(val scheduler: Scheduler): CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        scheduler.run { block.run() }
    }
}