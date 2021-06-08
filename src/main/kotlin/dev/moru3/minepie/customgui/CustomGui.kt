package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.CustomGuiClickEvent.Companion.asCustomGuiClickEvent
import dev.moru3.minepie.utils.IgnoreRunnable.Companion.ignoreException
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

open class CustomGui(plugin: Plugin, final override val title: String, final override val size: Int, runnable: CustomGui.() -> Unit = {}) : ICustomGui<CustomGui>, Listener {

    final override val isSync: Boolean = false
    protected val inventory: Inventory
    protected val actionItems: MutableList<ActionItem> = mutableListOf()

    override fun removeItem(itemStack: ItemStack): CustomGui {
        val items = actionItems.filter { it.itemStack==itemStack }.run { if(this.isEmpty()) { null } else { this } }?:return this
        inventory.remove(itemStack)
        items.map(actionItems::remove)
        return this
    }

    override fun setItem(itemStack: ItemStack, x: Int, y: Int, runnable: ActionItem.() -> Unit): CustomGui {
        if(x !in 0..8) { throw IndexOutOfBoundsException("size is not in the range of (0..8).") }
        if(y !in 0 until size) { throw IndexOutOfBoundsException("size is not in the range of (0..5).") }
        ActionItem(itemStack, slot = x+(y*9)).also {
            actionItems.filter { item -> item.itemStack==inventory.getItem(x+(y*9)) }.also { item ->
                if(item.size==1) { actionItems.remove(item[0]) }
            }
            actionItems.add(it)
            inventory.setItem(x+(y*9), itemStack.clone())
            runnable.invoke(it)
        }
        return this
    }

    override fun getItem(x: Int, y: Int, runnable: ActionItem.() -> Unit): ActionItem? {
        val item = inventory.getItem(x+(y*9))?:return null
        return actionItems.filter { it.itemStack==item }.run {
            if(this.isEmpty()) { null } else { this.first() }
        }
    }

    override fun removeItem(x: Int, y: Int): CustomGui {
        TODO("Not yet implemented")
    }

    final override fun asInventory(): Inventory {
        val result = Bukkit.createInventory(null, (size+1)*9, title)
        result.contents = inventory.contents.map(ItemStack::clone).toTypedArray()
        return result
    }

    override fun open(player: Player) {
        player.openInventory(this.asInventory())
    }

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        if(event.view.topInventory!=inventory) { return }
        actionItems.filter { it.itemStack == event.currentItem }.forEach { actionItem ->
            if(actionItem.isAllowGet) { event.isCancelled = true }
            actionItem.getActions().filterKeys(event.click::equals).values.forEach {
                it.invoke(event.asCustomGuiClickEvent(this))
            } }
    }

    /**
     * initは最後に置いておいてね
     */
    init {
        if(size !in 0..5) { throw IllegalArgumentException("size is not in the range of (0..5).") }
        inventory = Bukkit.createInventory(null, (size+1)*9, title)
        Runnable {
            Bukkit.getPluginManager().registerEvents(this, plugin)
            runnable.invoke(this)
        }.ignoreException()
    }

    companion object {
        fun Plugin.createCustomGui(size: Int, title: String, runnable: CustomGui.() -> Unit = {}): CustomGui {
            return CustomGui(this, title, size, runnable)
        }
    }
}