package dev.moru3.minepie.customgui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

class CustomSyncGui(plugin: Plugin, title: String, size: Int, runnable: CustomSyncGui.() -> Unit = {}): CustomGui(plugin, title, size) {
    override fun open(player: Player) {
        player.openInventory(this.asInventory())
    }

    override fun asInventory(): Inventory {
        return inventory
    }

    init {
        isSync = true
        runnable.invoke(this)
    }

    companion object {
        fun Plugin.createCustomSyncGui(size: Int, title: String, runnable: CustomSyncGui.() -> Unit = {}): CustomSyncGui {
            return CustomSyncGui(this, title, size, runnable)
        }
    }
}