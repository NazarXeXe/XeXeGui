package me.nazarxexe.ui.pagination

import me.nazarxexe.ui.*
import me.nazarxexe.ui.signals.signal
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

open class Pagination(val reserve: Set<Int>, gui: Gui) {

    val pages = mutableMapOf<Int, Page>()
    protected val currentComposable = mutableListOf<GuiComposable>()

    var page: Int
        get() = currentPage
        set(value) {
            changePage(value)
            currentPage = value
            pageSignal.value(value)
        }

    var pageSignal = signal(page)

    open fun createNewPage(at: Int): Page {
        val page = Page(this)
        page.currentSlot = at
        pages[at] = page
        return page
    }

    open fun createNewPage(at: Int, init: Page.() -> Unit): Page {
        val page = createNewPage(at)
        init(page)
        return page
    }

    open fun getPageAt(page: Int): Page? {
        return pages[page]
    }

    open fun getCurrentPage(): Page? {
        return pages[currentPage]
    }

    private var currentPage = 0

    open fun changePage(to: Int) {
        currentPage = to
        update()
    }

    open fun update() {
        val page = (pages[currentPage] ?: error("Page doesn't exist.")).iterator()
        paginationComponents.forEach {
            it.value.child = null
        }
        currentComposable.clear()
        page.forEach {
            paginationComponents[it.key]?.child = it.value
            it.value.signal()
            currentComposable.addAll(it.value.composable)
        }
        paginationComponents.forEach { it.value.signal() }
    }


    val paginationComponents = mutableMapOf<Int, PaginationComponent>()

    init {
        reserve.forEach {
            val component = PaginationComponent()
            paginationComponents[it] = component
            gui.component(it, component)
        }
        gui.compose { e ->
            currentComposable.forEach { it.react(e) }
        }
    }


}

class Page(val parent: Pagination) {
    internal var currentSlot = 0
    private val components = mutableMapOf<Int, GuiComponent>()

    fun set(slot: Int, component: GuiComponent) {
        if (!parent.reserve.contains(slot)) error("Out of bounds.")
        components[slot]?.changeSignal { }
        components[slot] = component
    }

    fun iterator(): Iterator<Map.Entry<Int, GuiComponent>> {
        return components.iterator()
    }



}

class PaginationComponent : GuiComponent() {
    var child: GuiComponent? = null
        set(value) {
            field?.changeSignal { }
            value?.changeSignal(this.signal)
            field = value
        }

    override fun render(): ItemStack {
        return child?.render() ?: ItemStack(Material.AIR)
    }

}


fun Gui.pagination(reserve: Set<Int>): Pagination {
    return Pagination(reserve, this)
}

fun Gui.pagination(reserve: IntRange): Pagination {
    return Pagination(reserve.toSet(), this)
}
