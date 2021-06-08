package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.CustomGuiClickEvent.Companion.asCustomGuiClickEvent
import dev.moru3.minepie.utils.IgnoreRunnable.Companion.ignoreException
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
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
class CustomContentsGui(plugin: Plugin, size: Int, title: String, private val startX: Int, private val startY: Int, private val endX: Int, private val endY: Int, private val runnable: CustomContentsGui.() -> Unit = {}) : CustomGui(plugin, title, size) {
    private val contents = mutableListOf<ActionItem>()

    fun addContents(itemStack: ItemStack, runnable: ActionItem.() -> Unit = {}): CustomContentsGui {
        ActionItem(itemStack).also {
            contents.add(it)
            runnable.invoke(it)
        }
        return this
    }

    fun addContents(actionItem: ActionItem, runnable: ActionItem.() -> Unit = {}): CustomContentsGui {
        contents.add(actionItem)
        runnable.invoke(actionItem)
        return this
    }

    fun removeContents(actionItem: ActionItem): CustomContentsGui {
        contents.remove(actionItem)
        return this
    }

    fun clearContents(): CustomContentsGui {
        contents.clear()
        return this
    }

    override fun open(player: Player) {
        open(player, 1)
    }

    fun open(player: Player, page: Int = 1) {
        val gui = this.clone()
        val useSlots = mutableListOf<Int>()
        for(x in startX..endX) {
            for(y in startY..endY) {
                if(gui.getItem(x, y)!=null) { continue }
                useSlots.add(x+(y*9))
            }
        }
        if(useSlots.size*(page-1)>contents.size) {
            open(player, page-1)
            return
        }

        player.openInventory(gui.asInventory())

        object: Listener {
            @EventHandler
            fun onInventoryClick(event: InventoryClickEvent) {
                if(event.view.topInventory!=inventory) { return }
                gui.actionItems.filter { it.slot == event.slot }.filter { it.itemStack == event.currentItem }.forEach { actionItem ->
                    if(actionItem.isAllowGet) { event.isCancelled = true }
                    actionItem.getActions().filterKeys(event.click::equals).values.forEach {
                        it.invoke(event.asCustomGuiClickEvent(this))
                    } }
            }

            @EventHandler
            fun onInventoryClose(event: InventoryCloseEvent) {
                if(event.player==player) {
                    HandlerList.unregisterAll(this)
                }
            }

            init {
                Bukkit.getPluginManager().registerEvents(this, plugin)
            }
        }
    }

    override fun clone(): CustomContentsGui {
        val customContentsGui = CustomContentsGui(plugin, size, title, startX, startY, endX, endY, runnable)
        for(x in 0..8) {
            for(y in 0..size) {
                customContentsGui.setItem(x, y, this.getItem(x, y)?.clone()?:continue)
            }
        }
        contents.forEach(customContentsGui::addContents)
        return customContentsGui
    }

    /**
     * initは最後に置いておいてね
     */
    init {
        Runnable { runnable.invoke(this) }.ignoreException()
    }

    companion object {
        fun Plugin.createCustomContentsGui(size: Int, title: String, startX: Int, startY: Int, endX: Int, endY: Int, runnable: CustomContentsGui.() -> Unit = {}): CustomContentsGui {
            return CustomContentsGui(this, size, title, startX, startY, endX, endY, runnable)
        }
    }
}