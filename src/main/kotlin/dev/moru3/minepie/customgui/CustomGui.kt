package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.CustomGuiClickEvent.Companion.asCustomGuiClickEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

/**
 * @param plugin JavaPluginを入れてください。
 * @param size インベントリの縦の列のサイズです。
 * @param title インベントリのタイトルを設定してください。
 * @param startX addItemをした際に(ry
 * @param startY addItemをした際(ry
 * @param endX addItemをした(ry
 * @param endY addItemをし(ry
 * @param runnable 任意:処理を記述してください
 */
class CustomGui(private val plugin: Plugin, private val size: Int, private val title: String, val startX: Int, val startY: Int, val endX: Int, val endY: Int, runnable: CustomGui.()->Unit = {}) {
    private val inventory: Inventory
    private val actionItems: MutableList<ActionItem> = mutableListOf()
    private val contents = mutableListOf<ItemStack>()

    /**
     * アクションアイテムを追加します。start..endの範囲内に収まります。
     */
    fun addItem(itemStack: ItemStack, runnable: ActionItem.() -> Unit = {}) {
        ActionItem(itemStack).also {
            actionItems.add(it)
            contents.add(it.itemStack)
            runnable.invoke(it)
        }
    }

    fun setItem(itemStack: ItemStack, x: Int, y: Int, runnable: ActionItem.() -> Unit = {}) {
        ActionItem(itemStack).also {
            actionItems.filter { item -> item.itemStack==inventory.getItem(x+(y*9)) }.also { item ->
                if(item.size==1) { actionItems.remove(item[0]) }
            }
            actionItems.add(it)
            inventory.setItem(x+(y*9), itemStack)
            runnable.invoke(it)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        actionItems.forEach { actionItem ->
            actionItem.getActions().filterKeys(event.click::equals).values.forEach { it.invoke(event.asCustomGuiClickEvent(this)) }
        }
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
        fun Plugin.createCustomGui(size: Int, title: String, runnable: CustomGui.() -> Unit = {}): CustomGui {
            return CustomGui(this, size, title, 0, 0, 8, 5, runnable)
        }
    }
}