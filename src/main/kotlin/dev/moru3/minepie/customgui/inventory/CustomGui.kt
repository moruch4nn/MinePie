package dev.moru3.minepie.customgui.inventory

import dev.moru3.minepie.customgui.ActionItem
import dev.moru3.minepie.customgui.CustomGuiEventListener
import dev.moru3.minepie.customgui.CustomGuiEvents
import dev.moru3.minepie.customgui.ICustomGui
import dev.moru3.minepie.events.CustomGuiClickEvent.Companion.asCustomGuiClickEvent
import dev.moru3.minepie.utils.IgnoreRunnable.Companion.ignoreException
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

open class CustomGui(protected val plugin: JavaPlugin, final override val title: String, final override val size: Int, private val runnable: CustomGui.() -> Unit = {}) : ICustomGui, Listener {
    final override var isSync: Boolean = false
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

    override fun removeItem(itemStack: ItemStack) {
        val items = actionItems.filter { it.itemStack==itemStack }.run { this.ifEmpty { return } }
        inventory.remove(itemStack)
        items.map(actionItems::remove)
    }

    override fun setItem(x: Int, y: Int, actionItem: ActionItem?, runnable: ActionItem.() -> Unit) {
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
    }

    override fun setItem(x: Int, y: Int, itemStack: ItemStack?, runnable: ActionItem.() -> Unit) {
        if(x !in 0..8) { throw IndexOutOfBoundsException("size is not in the range of (0..8).") }
        if(y !in 0..size) { throw IndexOutOfBoundsException("size is not in the range of (0..$size).") }
        removeItem(x, y)
        if(itemStack!=null) {
            ActionItem(itemStack, slot = x + (y * 9)).also {
                actionItems.add(it)
                inventory.setItem(x + (y * 9), itemStack.clone())
                runnable.invoke(it)
            }
        }
    }

    override fun getItem(x: Int, y: Int, runnable: ActionItem?.() -> Unit): ActionItem? {
        return actionItems.filter { it.slot==x*(y*9) }.let { if(it.isEmpty()) null else it.first() }.also { runnable.invoke(it) }
    }

    override fun removeItem(x: Int, y: Int) {
        actionItems.remove(getItem(x, y))
        inventory.setItem(x+(y*9), null)
    }

    override fun asInventory(): Inventory {
        val result = Bukkit.createInventory(null, (size+1)*9, title)
        result.contents = inventory.contents.map { it?.clone() }.toTypedArray()
        return result
    }

    override fun asRawInventory(): Inventory {
        return inventory
    }

    override fun replace(iCustomGui: ICustomGui) {
        for(x in 0..8) {
            for(y in 0..iCustomGui.size) {
                iCustomGui.getItem(x, y).also { this.setItem(x, y, it) }
            }
        }
    }

    override fun open(player: Player) {
        val gui = this.clone()
        player.openInventory(gui.asRawInventory())

        val listener = object: CustomGuiEvents() {
            override val javaPlugin = plugin

            override fun onInventoryClick(event: InventoryClickEvent) {
                if(event.whoClicked==player&&event.view.topInventory==gui.asRawInventory()) {
                    gui.actionItems.filter { it.slot==event.slot }
                        .filter { it.itemStack==event.currentItem }.forEach { actionItem ->
                            if(!actionItem.isAllowGet) { event.isCancelled = true }
                            actionItem.getActions().filter { it.key==event.click }.forEach {
                                it.value.invoke(event.asCustomGuiClickEvent(gui))
                            }
                        }
                }
            }
        }

        CustomGuiEventListener.register(listener)
    }

    /**
     * initは最後に置いておいてね
     */
    init {
        if(size !in 0..5) { throw IllegalArgumentException("size is not in the range of (0..5).") }
        inventory = Bukkit.createInventory(null, (size+1)*9, title)
        Runnable {
            runnable.invoke(this)
        }.ignoreException()
    }

    companion object {
        fun JavaPlugin.createCustomGui(size: Int, title: String, runnable: CustomGui.() -> Unit = {}): CustomGui {
            return CustomGui(this, title, size, runnable)
        }
    }
}