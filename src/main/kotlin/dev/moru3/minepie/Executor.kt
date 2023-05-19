package dev.moru3.minepie

import org.bukkit.conversations.PluginNameConversationPrefix
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.function.Consumer

class Executor {
    companion object {
        fun Plugin.loop(delay: Long,period: Long,count: Long,consumer: ()->Unit): BukkitTask {
            var c = 0L
            return this.runTaskTimer(0,period) {
                consumer.invoke()
                c++
            }
        }
        fun Plugin.runTask(consumer: ()->Unit): BukkitTask {
            return this.server.scheduler.runTask(this,consumer)
        }
        fun Plugin.runTaskLater(delay: Long,consumer: () -> Unit): BukkitTask {
            return this.server.scheduler.runTaskLater(this,consumer,delay)
        }
        fun Plugin.runTaskAsync(consumer: () -> Unit): BukkitTask {
            return this.server.scheduler.runTaskAsynchronously(this,consumer)
        }
        fun Plugin.runTaskLaterAsync(delay: Long,consumer: () -> Unit): BukkitTask {
            return this.server.scheduler.runTaskLaterAsynchronously(this,consumer,delay)
        }
        fun Plugin.runTaskTimerAsync(delay: Long, period: Long,consumer: () -> Unit): BukkitTask {
            return this.server.scheduler.runTaskTimerAsynchronously(this, consumer, delay, period)
        }
        fun Plugin.runTaskTimer(delay: Long,period: Long,consumer: () -> Unit): BukkitTask {
            return this.server.scheduler.runTaskTimer(this,consumer,delay,period)
        }
    }
}