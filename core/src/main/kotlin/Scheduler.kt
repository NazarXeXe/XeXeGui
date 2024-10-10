package me.nazarxexe.ui

import org.bukkit.scheduler.BukkitTask

interface Scheduler {
    fun run(runnable: () -> Unit): BukkitTask
    fun runRepeat(repeat: Int = 1, runnable: () -> Unit): BukkitTask
}