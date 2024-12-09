package me.nazarxexe.ui.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.nazarxexe.ui.ClosableState
import me.nazarxexe.ui.ComponentState
import me.nazarxexe.ui.GuiComponentBuilder
import me.nazarxexe.ui.Scheduler

fun <T> GuiComponentBuilder.asyncState(scheduler: Scheduler,
                   default: T,
                   scope: CoroutineScope = CoroutineScope(Dispatchers.IO), impl: suspend () -> T): ComponentState<T> {
    val mutex = mutexStateBuilder(scheduler, default)
    val job = scope.launch {
        mutex.set(impl())
    }
    val state = object : ComponentState<T>(), ClosableState {
        override fun get(): T = mutex.get()
        override fun set(value: T) = mutex.set(value)
        override fun close() = job.cancel()
    }
    stateList.add(state)
    return state
}

fun <T> GuiComponentBuilder.asyncState(scheduler: Scheduler,
                                       default: T,
                                       scope: CoroutineScope = CoroutineScope(Dispatchers.IO), impl: suspend (mutexState: ComponentState<T>) -> Unit): ComponentState<T> {
    val mutex = mutexStateBuilder(scheduler, default)
    val job = scope.launch {
        impl(mutex)
    }
    val state = object : ComponentState<T>(), ClosableState {
        override fun get(): T = mutex.get()
        override fun set(value: T) = mutex.set(value)
        override fun close() = job.cancel()
    }
    stateList.add(state)
    return state
}

fun <T> GuiComponentBuilder.mutexState(scheduler: Scheduler,
                                   default: T): ComponentState<T> {
    val mutex = mutexStateBuilder(scheduler, default)
    stateList.add(mutex)
    return mutex
}

fun <T> mutexStateBuilder(scheduler: Scheduler,
               default: T): ComponentState<T> {
    var o: T = default
    return object : ComponentState<T>() {
        override fun get(): T = o
        override fun set(value: T) {
            o = value
            scheduler.run {
                signal()
            }
        }
    }
}