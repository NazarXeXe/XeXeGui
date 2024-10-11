package me.nazarxexe.ui

object LinearCurve : (Float) -> Float {
    override fun invoke(t: Float): Float {
        return 1 - (2 * t)
    }
}
