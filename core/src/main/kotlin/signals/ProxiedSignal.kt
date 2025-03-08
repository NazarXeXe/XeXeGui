package me.nazarxexe.ui.signals

import java.lang.reflect.Method
import java.lang.reflect.Proxy


fun interface MethodHandle {
    /**
     * @return true if you call the handlers
     */
    fun handle(method: Method): Boolean
}

class ProxiedSignal<T>(
    var default: T,
    val methodHandle: MethodHandle
): Signal<T> {
    override val hooks: MutableList<() -> Unit> = mutableListOf()
    var proxied: T? = null
    override fun addHook(hook: () -> Unit) {
        hooks.add(hook)
    }

    override fun value(): T {
        if (default == null) return default
        if (proxied != null)
            return proxied!!
        else {
            proxy()
            return proxied!!
        }
    }

    fun proxy() {
        val clazz = default!!::class.java
        proxied = Proxy.newProxyInstance(clazz.classLoader, clazz.interfaces
        ) { _, method, args ->
            val o: Any? = if (args == null) {
                method.invoke(default)
            } else {
                method.invoke(default, *args)
            }
            if (methodHandle.handle(method)) {
                hooks.forEach { it() }
            }
            o
        } as T
    }

    override fun value(value: T) {
        default = value
        if (default != null) proxy()
    }
}

fun <T> proxiedSignal(default: T, methodHandle: MethodHandle = MethodHandle {
    it.name.startsWith("set")
}): ProxiedSignal<T> {
    return ProxiedSignal(default, methodHandle)
}
