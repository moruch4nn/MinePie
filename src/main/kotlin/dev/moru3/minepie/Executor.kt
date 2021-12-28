package dev.moru3.minepie

import org.bukkit.conversations.PluginNameConversationPrefix
import org.bukkit.plugin.Plugin
import java.util.function.Consumer

class Executor {
    companion object {
        fun Plugin.runTask(consumer: ()->Unit) {
            this.server.scheduler.runTask(this,consumer)
        }
        fun Plugin.runTaskLater(delay: Long,consumer: () -> Unit) {
            this.server.scheduler.runTaskLater(this,consumer,delay)
        }
        fun Plugin.runTaskAsync(consumer: () -> Unit) {
            this.server.scheduler.runTaskAsynchronously(this,consumer)
        }
        fun Plugin.runTaskLaterAsync(delay: Long,consumer: () -> Unit) {
            this.server.scheduler.runTaskLaterAsynchronously(this,consumer,delay)
        }
    }
}