/*
 * Copyright (c) 2021. moru3_48. All Right Reserved.
 */

package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.EventRegister
import dev.moru3.minepie.utils.Utils.Companion.isNull
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * Bukkitデフォルトのイベントが思ったより重そうだったのでここにハブを作ります。
 */
class CustomGuiEventListener: Listener {
    companion object {
        private val listeners = mutableMapOf<UUID, CustomGuiEvents>()

        var createdListener = false

        private fun registerNewListener(javaPlugin: JavaPlugin) {
            createdListener = true
            EventRegister(javaPlugin) {
                register { event: InventoryClickEvent ->
                    val holder = event.view.topInventory.holder
                    if(holder is UniqueInventoryHolder) {
                        listeners[holder.uniqueId]?.onInventoryClick(event)
                    }
                }
                register { event: PlayerQuitEvent ->
                    val holder = event.player.openInventory.topInventory.holder
                    if(holder is UniqueInventoryHolder) {
                        listeners[holder.uniqueId]?.onPlayerQuit(event)
                    }
                }
                register { event: InventoryCloseEvent ->
                    val holder = event.view.topInventory.holder
                    if(holder is UniqueInventoryHolder) {
                        listeners[holder.uniqueId]?.onInventoryClose(event)
                    }
                }
            }
        }

        fun register(customGuiEvents: CustomGuiEvents) {
            listeners[customGuiEvents.uniqueInventoryHolder.uniqueId] = customGuiEvents
            if(!createdListener) { registerNewListener(customGuiEvents.javaPlugin) }
        }

        fun unregister(customGuiEvents: CustomGuiEvents) {
            listeners.remove(customGuiEvents.uniqueInventoryHolder.uniqueId)
        }
    }
}

abstract class CustomGuiEvents: ICustomGuiEvents {
    /**
     * プレイヤーがインベントリをクリックした際に呼ぶ出されます。
     */
    override fun onInventoryClick(event: InventoryClickEvent) { /** パス **/ }

    /**
     * プレイヤーがサーバーから退出した際に呼び出されます。
     * デフォルトでは呼び出された際にCustomGuiEventListenerからunregisterします。
     */
    override fun onPlayerQuit(event: PlayerQuitEvent) { CustomGuiEventListener.unregister(this) }

    /**
     * プレイヤーがインベントリを閉じた際に呼び出されます。
     * デフォルトでは呼出sれた際にCustomGuiEventListenerからunregisterします。
     */
    override fun onInventoryClose(event: InventoryCloseEvent) { CustomGuiEventListener.unregister(this) }
}

interface ICustomGuiEvents {
    val javaPlugin: JavaPlugin

    val uniqueInventoryHolder: UniqueInventoryHolder

    /**
     * プレイヤーがインベントリをクリックした際に呼ぶ出されます。
     */
    fun onInventoryClick(event: InventoryClickEvent)

    /**
     * プレイヤーがサーバーから退出した際に呼び出されます。
     */
    fun onPlayerQuit(event: PlayerQuitEvent)

    /**
     * プレイヤーがインベントリを閉じた際に呼び出されます。
     */
    fun onInventoryClose(event: InventoryCloseEvent)
}