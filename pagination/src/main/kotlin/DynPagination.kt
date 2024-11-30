package me.nazarxexe.ui.pagination

import me.nazarxexe.ui.Gui

class DynPagination(
    reserve: Set<Int>,
    gui: Gui,
    val pageSupplier: DynPagination.(Int) -> Page,
    val cache: Boolean = false
) : Pagination(reserve, gui) {
    override fun changePage(to: Int) {
        if (pages[to] != null && cache) {
            super.changePage(to)
            return
        }
        if (!cache)
            pages.clear() // If it's not cached there's no point saving pages.
        pages[to] = pageSupplier(this, to)
        super.changePage(to)
    }
}

fun Gui.dynPagination(reserve: Set<Int>, supplier: DynPagination.(Int) -> Page): DynPagination {
    return DynPagination(reserve, this, pageSupplier = supplier)
}

fun Gui.dynPagination(reserve: IntRange, supplier: DynPagination.(Int) -> Page): DynPagination {
    return DynPagination(reserve.toSet(), this, pageSupplier = supplier)
}

fun Gui.dynPagination(reserve: IntRange, cache: Boolean, supplier: DynPagination.(Int) -> Page): DynPagination {
    return DynPagination(reserve.toSet(), this, pageSupplier = supplier, cache = cache)
}