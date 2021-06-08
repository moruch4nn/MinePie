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
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

open class CustomGui(protected val plugin: Plugin, final override val title: String, final override val size: Int, private val runnable: CustomGui.() -> Unit = {}) : ICustomGui<CustomGui>, Listener {

    final override var isSync: Boolean = true
    protected set

    protected val inventory: Inventory

    protected val actionItems: MutableList<ActionItem> = mutableListOf()

    override fun clone(): CustomGui {
        val customGui = CustomGui(plugin, title, size, runnable)
        for(x in 0..8) {
            for(y in 0..size) {
                customGui.setItem(x, y, this.getItem(x, y)?.clone()?:continue)
            }
        }
        return customGui
    }

    override fun removeItem(itemStack: ItemStack): CustomGui {
        val items = actionItems.filter { it.itemStack==itemStack }.run { if(this.isEmpty()) { null } else { this } }?:return this
        inventory.remove(itemStack)
        items.map(actionItems::remove)
        return this
    }

    override fun setItem(x: Int, y: Int, actionItem: ActionItem?, runnable: ActionItem.() -> Unit): CustomGui {
        if(x !in 0..8) { throw IndexOutOfBoundsException("size is not in the range of (0..8).") }
        if(y !in 0..size) { throw IndexOutOfBoundsException("size is not in the range of (0..$size).") }
        if(actionItem==null) {
            removeItem(x, y)
        } else {
            val item = actionItem.clone()
            removeItem(x, y)
            actionItems.add(item)
            inventory.setItem(x+(y*9), item.itemStack)
            runnable.invoke(item)
        }
        return this
    }

    override fun setItem(x: Int, y: Int, itemStack: ItemStack?, runnable: ActionItem.() -> Unit): CustomGui {
        if(x !in 0..8) { throw IndexOutOfBoundsException("size is not in the range of (0..8).") }
        if(y !in 0..size) { throw IndexOutOfBoundsException("size is not in the range of (0..$size).") }
        if(itemStack==null) {
            removeItem(x, y)
        } else {
            ActionItem(itemStack, slot = x + (y * 9)).also {
                removeItem(x, y)
                actionItems.add(it)
                inventory.setItem(x + (y * 9), itemStack.clone())
                runnable.invoke(it)
            }
        }
        return this
    }

    override fun getItem(x: Int, y: Int, runnable: ActionItem.() -> Unit): ActionItem? {
        return actionItems.filter { it.slot==x*(y*9) }.let { if(it.isEmpty()) null else it.first() }
    }

    override fun removeItem(x: Int, y: Int): CustomGui {
        actionItems.remove(getItem(x, y))
        inventory.setItem(x+(y*9), null)
        return this
    }

    override fun asInventory(): Inventory {
        val result = Bukkit.createInventory(null, (size+1)*9, title)
        result.contents = inventory.contents.map(ItemStack::clone).toTypedArray()
        return result
    }

    /**
     * インベントリを開きます。(non-sync)
     */
    override fun open(player: Player) {
        val gui = this.clone()
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