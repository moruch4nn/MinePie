/*
 * Copyright (c) 2021. moru3_48. All Right Reserved.
 */

package dev.moru3.minepie.customgui.inventory

import dev.moru3.minepie.customgui.CustomGuiEventListener
import dev.moru3.minepie.customgui.CustomGuiEvents
import dev.moru3.minepie.events.CustomGuiClickEvent.Companion.asCustomGuiClickEvent
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * setItemや、アイテムを取る、入れるなどの動作がリアルタイムで反映されます。
 * チェストやかまどなどのリアルタイムで反映が必要なGUIに使用できます。
 * 注: 一度作るとリスナーを削除できません。
 */
class CustomSyncGui(plugin: JavaPlugin, title: String, size: Int, runnable: CustomSyncGui.() -> Unit = {}): CustomGui(plugin, title, size) {

    private val listener: CustomGuiEvents

    /**
     * リスナーを削除します。再生成するにはCustomSyncGuiを再度作成してください。
     */
    fun unregisterGuiListener() {
        CustomGuiEventListener.unregister(listener)
    }

    override fun open(player: Player) {
        player.openInventory(this.asRawInventory())
    }

    init {
        isSync = true
        runnable.invoke(this)
        listener = object: CustomGuiEvents() {
            override val javaPlugin = plugin
            override fun onInventoryClick(event: InventoryClickEvent) {
                if(event.view.topInventory==this@CustomSyncGui.asRawInventory()) {
                    this@CustomSyncGui.actionItems.filter { it.slot==event.slot }
                        .filter { it.itemStack==event.currentItem }.forEach { actionItem ->
                            if(!actionItem.isAllowGet) { event.isCancelled = true }
                            actionItem.getActions().filter { it.key==event.click }.forEach {
                                it.value.invoke(event.asCustomGuiClickEvent(this@CustomSyncGui))
                            } }
                } }
        }
        CustomGuiEventListener.register(listener)
    }

    companion object {
        /**
         * CustomGuiをCustomSyncGuiにCastします。
         */
        fun CustomGui.asSync(runnable: CustomSyncGui.() -> Unit = {}): CustomSyncGui {
            val javaPlugin = this::class.java.getDeclaredField("plugin").also { it.isAccessible = true }.get(this) as JavaPlugin
            return CustomSyncGui(javaPlugin, this.title, this.size, runnable).also {
                for(x in 0..8) { for(y in 0..this.size) { it.setItem(x, y, this.getItem(x, y)?.clone()) } }
            }
        }
    }
}