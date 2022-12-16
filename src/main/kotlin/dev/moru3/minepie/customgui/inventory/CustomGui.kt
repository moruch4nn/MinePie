package dev.moru3.minepie.customgui.inventory

import dev.moru3.minepie.customgui.*
import dev.moru3.minepie.events.CustomGuiClickEvent.Companion.asCustomGuiClickEvent
import dev.moru3.minepie.utils.IgnoreRunnable.Companion.ignoreException
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

open class CustomGui(protected val plugin: JavaPlugin, final override val title: String, final override val size: Int, private val runnable: CustomGui.() -> Unit = {}) : ICustomGui, Listener {
    final override var isSync: Boolean = false
    protected set

    final override val uniqueTagKey: NamespacedKey = NamespacedKey(plugin, "CustomGui.${UUID.randomUUID()}")

    protected val inventory: Inventory

    protected val actionItems: MutableList<ActionItem> = mutableListOf()

    protected val closeProcesses = mutableListOf<(InventoryCloseEvent)->Unit>()

    final override val uniqueInventoryHolder: UniqueInventoryHolder = UniqueInventoryHolder()

    override fun addCloseListener(process: (InventoryCloseEvent) -> Unit) { closeProcesses.add(process) }



    override fun clone(): CustomGui {
        val customGui = CustomGui(plugin, title, size) { }
        actionItems.forEach {
            customGui.set(it.slot!!%9,it.slot!!/9,it)
        }
        return customGui
    }

    override fun remove(itemStack: ItemStack) {
        val items = actionItems.filter { it.itemStack==itemStack }.run { this.ifEmpty { return } }
        inventory.remove(itemStack)
        items.map(actionItems::remove)
    }

    override fun remove(x: Int, y: Int) {
        actionItems.removeAll { it.slot==x+(y*9) }
        inventory.setItem(x+(y*9), null)
    }

    override fun set(x: Int, y: Int, actionItem: ActionItem?, runnable: ActionItem.() -> Unit) {
        if(x !in 0..8) { throw IndexOutOfBoundsException("size is not in the range of (0..8).") }
        if(y !in 0..size) { throw IndexOutOfBoundsException("size is not in the range of (0..$size).") }
        if(actionItem==null) {
            this.remove(x, y)
        } else {
            if(this.get(x,y)!=null) { this.remove(x,y) }
            val item = actionItem.copy(x+(y*9))
            actionItems.add(item)
            inventory.setItem(x+(y*9), item.itemStack)
            runnable.invoke(item)
        }
    }

    override fun set(x: Int, y: Int, itemStack: ItemStack?, runnable: ActionItem.() -> Unit) {
        if(x !in 0..8) { throw IndexOutOfBoundsException("size is not in the range of (0..8).") }
        if(y !in 0..size) { throw IndexOutOfBoundsException("size is not in the range of (0..$size).") }
        remove(x, y)
        if(itemStack!=null) {
            set(x,y,ActionItem(uniqueTagKey = uniqueTagKey,itemStack, slot = x+(y*9)),runnable)
        }
    }

    override fun get(x: Int, y: Int, runnable: ActionItem.() -> Unit): ActionItem? {
        return actionItems.filter { it.slot==x+(y*9) }.getOrNull(0)?.also(runnable::invoke)
    }

    override fun asInventory(): Inventory {
        val result = Bukkit.createInventory(UniqueInventoryHolder(), (size)*9, title)
        result.contents = inventory.contents.map { it?.clone() }.toTypedArray()
        return result
    }

    override fun asRawInventory(): Inventory {
        return inventory
    }

    override fun replace(iCustomGui: ICustomGui) {
        for(x in 0..8) {
            for(y in 0..iCustomGui.size) {
                iCustomGui.get(x, y).also { this.set(x, y, it) }
            }
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        closeProcesses.forEach { it.invoke(event) }
    }

    override fun open(player: Player) {
        val gui = this.clone()
        player.openInventory(gui.asRawInventory())

        val listener = object: CustomGuiEvents() {
            override val javaPlugin = plugin
            override val uniqueInventoryHolder: UniqueInventoryHolder = gui.uniqueInventoryHolder
            override fun onInventoryClick(event: InventoryClickEvent) {
                gui.actionItems.filter { it.isSimilarTag(event.currentItem) }.forEach { actionItem ->
                        if(!actionItem.isAllowGet) {
                            event.isCancelled = true
                            (event.whoClicked as Player).playSound(event.whoClicked.location, actionItem.clickSound,1F,1F)
                        }
                        actionItem.getActions().filter { it.key==event.click }.forEach {
                            it.value.invoke(event.asCustomGuiClickEvent(gui))
                        }
                    }
            }

            override fun onInventoryClose(event: InventoryCloseEvent) {
                if(event.player==player) { onClose(event) }
            }
        }

        CustomGuiEventListener.register(listener)
    }

    /**
     * initは最後に置いておいてね
     */
    init {
        if(size !in 1..6) { throw IllegalArgumentException("size is not in the range of (1..6).") }
        inventory = Bukkit.createInventory(uniqueInventoryHolder, (size)*9, title)
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