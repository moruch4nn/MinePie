package dev.moru3.minepie

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

class Executor {
    companion object {
        fun Plugin.runTask(consumer: (BukkitTask)->Unit): BukkitTask {
            var bukkitTask: BukkitTask? = null
            bukkitTask = this.server.scheduler.runTask(this, Runnable { consumer.invoke(bukkitTask!!) })
            return bukkitTask
        }
        fun Plugin.runTaskLater(delay: Long,consumer: (BukkitTask) -> Unit): BukkitTask {
            var bukkitTask: BukkitTask? = null
            bukkitTask = this.server.scheduler.runTaskLater(this, Runnable { consumer.invoke(bukkitTask!!) }, delay)
            return bukkitTask
        }
        fun Plugin.runTaskAsync(consumer: (BukkitTask) -> Unit): BukkitTask {
            var bukkitTask: BukkitTask? = null
            bukkitTask = this.server.scheduler.runTaskAsynchronously(this, Runnable { consumer.invoke(bukkitTask!!) })
            return bukkitTask
        }
        fun Plugin.runTaskLaterAsync(delay: Long,consumer: (BukkitTask) -> Unit): BukkitTask {
            var bukkitTask: BukkitTask? = null
            bukkitTask = this.server.scheduler.runTaskLaterAsynchronously(this, Runnable { consumer.invoke(bukkitTask!!) }, delay)
            return bukkitTask
        }
        fun Plugin.runTaskTimerAsync(delay: Long, period: Long,consumer: (BukkitTask,Long) -> Unit): BukkitTask {
            var bukkitTask: BukkitTask? = null
            var count = 0L
            bukkitTask = this.server.scheduler.runTaskTimer(this, Runnable { consumer.invoke(bukkitTask!!,count++) }, delay, period)
            return bukkitTask
        }
        fun Plugin.runTaskTimer(delay: Long,period: Long,consumer: (BukkitTask,Long) -> Unit): BukkitTask {
            var bukkitTask: BukkitTask? = null
            var count = 0L
            bukkitTask = this.server.scheduler.runTaskTimer(this, Runnable { consumer.invoke(bukkitTask!!,count++) }, delay, period)
            return bukkitTask
        }
    }
}