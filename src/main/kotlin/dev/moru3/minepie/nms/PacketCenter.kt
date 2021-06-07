package dev.moru3.minepie.nms

import dev.moru3.minepie.events.AsyncPacketReadEvent
import dev.moru3.minepie.events.AsyncPacketWriteEvent
import dev.moru3.minepie.nms.NmsUtils.Companion.asNmsPlayer
import dev.moru3.minepie.utils.IgnoreRunnable
import dev.moru3.minepie.utils.IgnoreRunnable.Companion.ignoreException
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class PacketCenter(val main: JavaPlugin): IgnoreRunnable, Listener {
    init {
        Bukkit.getOnlinePlayers().forEach(this::createNewPipeline)
        Bukkit.getPluginManager().registerEvents(this, main)
    }

    private fun createNewPipeline(player: Player) { player.createNewPipeline() }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.createNewPipeline()
    }

    fun Player.createNewPipeline(): Boolean {
        return Runnable {
            val channelHandlerAdapter = object : ChannelHandlerAdapter() {
                override fun channelRead(ctx: ChannelHandlerContext?, packet: Any?) {
                    if(packet!=null) {
                        val event = AsyncPacketReadEvent(this@createNewPipeline, packet)
                        Bukkit.getPluginManager().callEvent(event)
                        if(event.isCancelled) { return }
                    }
                    super.channelRead(ctx, packet)
                }

                override fun write(ctx: ChannelHandlerContext?, packet: Any?, promise: ChannelPromise?) {
                    Bukkit.getScheduler().runTaskAsynchronously(main, Runnable s@{
                        if(packet!=null) {
                            val event = AsyncPacketWriteEvent(this@createNewPipeline, packet)
                            Bukkit.getPluginManager().callEvent(event)
                            if(event.isCancelled) { return@s }
                        }
                        super.write(ctx, packet, promise)
                    })
                }
            }
            this.asNmsPlayer()
                .run { this::class.java.getField("playerConnection").also { it.isAccessible = true }[this] }
                .run { this::class.java.getField("networkManager").also { it.isAccessible = true }[this] }
                .also { any ->
                    if(any::class.java.getMethod("get", String::class.java)
                            .also { it.isAccessible = true}.invoke(any, "${this.name}-minepie-pipeline")!=null) {
                        any::class.java.getMethod("remove", String::class.java).invoke(any, "${this.name}-minepie-pipeline")
                    }
                }.run {
                    this::class.java.getMethod("addBefore", String::class.java, String::class.java, ChannelHandler::class.java)
                        .also { it.isAccessible = true}
                        .invoke(this,"packet_handler", "${this@createNewPipeline.name}-runformoney_reloaded", channelHandlerAdapter)
                }
        }.ignoreException()
    }
}