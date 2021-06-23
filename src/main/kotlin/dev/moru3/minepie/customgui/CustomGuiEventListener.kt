/*
 * Copyright (c) 2021. moru3_48. All Right Reserved.
 */

package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.EventRegister
import dev.moru3.minepie.utils.Utils.Companion.isNull
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * Bukkitデフォルトのイベントが思ったより重そうだったのでここにハブを作ります。
 */
class CustomGuiEventListener: Listener {
    companion object {
        private val listeners = mutableMapOf<JavaPlugin, MutableList<CustomGuiEvents>>()

        private fun registerNewListener(javaPlugin: JavaPlugin) {
            EventRegister(javaPlugin) {
                register { event: InventoryClickEvent -> listeners[javaPlugin]?.forEach { it.onInventoryClick(event) } }
                register { event: PlayerQuitEvent -> listeners[javaPlugin]?.forEach { it.onPlayerQuit(event) } }
                register { event: InventoryCloseEvent -> listeners[javaPlugin]?.forEach { it.onInventoryClose(event) }}
            }
        }

        fun register(customGuiEvents: CustomGuiEvents) {
            val listenerList = listeners[customGuiEvents.javaPlugin].isNull{
                registerNewListener(customGuiEvents.javaPlugin)
                mutableListOf()
            }
            listenerList.add(customGuiEvents)
            listeners[customGuiEvents.javaPlugin] = listenerList
        }

        fun unregister(customGuiEvents: CustomGuiEvents) {
            listeners[customGuiEvents.javaPlugin]?.remove(customGuiEvents)
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