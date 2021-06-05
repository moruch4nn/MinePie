package dev.moru3.minepie.inventory

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class InventoryGui(private val plugin: Plugin, private val size: Int, private val title: String, private val runnable: InventoryGui.()->Unit) {
    private val inventory: Inventory
    private val actionItems: MutableList<ActionItem> = mutableListOf()

    /**
     * アクションアイテムを追加します
     */
    fun addItem(itemStack: ItemStack, runnable: ActionItem.() -> Unit = {}) {
        ActionItem(itemStack).also {
            runnable.invoke(it)
            actionItems.add(it)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {

    }

    /**
     * initは最後に置いておいてね
     */
    init {
        if(size !in 1..6) { throw IllegalArgumentException("size is not in the range of 0..6.") }
        inventory = Bukkit.createInventory(null, size*9, title)

        runnable.invoke(this)
    }

    companion object {
        fun Plugin.createInventoryGui(size: Int, title: String, runnable: InventoryGui.() -> Unit = {}): InventoryGui {
            return InventoryGui(this, size, title, runnable)
        }
    }
}