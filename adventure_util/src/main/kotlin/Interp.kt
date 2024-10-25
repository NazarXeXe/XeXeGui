package me.nazarxexe.ui

object Interp : (Float) -> Float {
    override fun invoke(t: Float): Float {
        return 1 - (2 * t)
    }
}
