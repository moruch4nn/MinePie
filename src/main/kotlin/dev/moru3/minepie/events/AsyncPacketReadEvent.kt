package dev.moru3.minepie.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * 非同期で実行されるパケットリスナーイベントです。
 */
class AsyncPacketReadEvent(val player: Player, val packet: Any): Event(true), Cancellable {

    private var cancelled = false

    private val handlers = HandlerList()

    override fun getHandlers(): HandlerList { return handlers }

    override fun isCancelled(): Boolean { return cancelled }

    override fun setCancelled(p0: Boolean) { cancelled = p0 }
}