package dev.moru3.minepie.customgui.inventory

import dev.moru3.minepie.customgui.*
import dev.moru3.minepie.events.CustomGuiClickEvent.Companion.asCustomGuiClickEvent
import dev.moru3.minepie.utils.IgnoreRunnable.Companion.ignoreException
import dev.moru3.minepie.utils.Utils.Companion.isNull
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

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
open class CustomContentsSyncGui(plugin: JavaPlugin, size: Int, title: String, private val startX: Int, private val startY: Int, private val endX: Int, private val endY: Int, private val runnable: CustomContentsSyncGui.() -> Unit = {}): CustomGui(plugin, title, size) {
    private val contents = mutableListOf<ActionItem>()
    private val openInventory = super.asInventory()
    private val openInventoryHolder = openInventory.holder as UniqueInventoryHolder
    private var isSingleton = true
    private var index: Int = 0
    private val indexes = mutableListOf<Int>()

    var page = 1
        private set(value) { field = maxOf(1,value) }

    private var sort: (ActionItem)->Comparable<*>? = SORT_BY_DISPLAY_NAME

    override fun setItem(x: Int, y: Int, actionItem: ActionItem?, runnable: ActionItem.() -> Unit) {
        super.setItem(x, y, actionItem, runnable)
        update()
    }

    override fun setItem(x: Int, y: Int, itemStack: ItemStack?, runnable: ActionItem.() -> Unit) {
        super.setItem(x, y, itemStack, runnable)
        update()
    }

    override fun removeItem(x: Int, y: Int) {
        super.removeItem(x, y)
        update()
    }

    override fun removeItem(itemStack: ItemStack) {
        super.removeItem(itemStack)
        update()
    }

    override fun replace(iCustomGui: ICustomGui) {
        super.replace(iCustomGui)
        update()
    }

    open fun addContents(itemStack: ItemStack, update: Boolean = true, runnable: ActionItem.() -> Unit = {}): CustomContentsSyncGui {
        actionItems.add(ActionItem(itemStack.clone()).also(contents::add).also(runnable::invoke))
        return this
    }

    fun setAutoClose(bool: Boolean) {
        isSingleton = bool
    }

    open fun addContents(actionItem: ActionItem, update: Boolean = true, runnable: ActionItem.() -> Unit = {}): CustomContentsSyncGui {
        addContents(actionItem.itemStack.clone()) {
            actionItem.getActions().forEach(this::addAction)
            runnable.invoke(this)
        }
        if(update) { update() }
        return this
    }

    private fun update(page: Int? = null) {
        index = 0
        indexes.clear()
        for(y in startY..endY) { for(x in startX..endX) {
            if(inventory.getItem(x+(y*9))==null) {
                indexes.add(x+(y*9))
                index++
            }
        } }
        page?.also { this.page = minOf(maxOf(it,1),this.contents.size/index) }
        val contents = this.contents.subList((this.page-1)*index, maxOf(0,this.contents.size))
        openInventory.contents = inventory.contents
        indexes.forEachIndexed { index2, i ->
            openInventory.setItem(i,contents.getOrNull(index2)?:return@forEachIndexed)
        }
    }

    fun replaceContents(old: ItemStack, new: ItemStack, runnable: ActionItem.() -> Unit = {}): CustomContentsSyncGui {
        repeat(removeContents(old)) { addContents(new, false, runnable::invoke) }
        update()
        return this
    }

    fun next() {
        update(page+1)
    }

    fun back() {
        update(page-1)
    }

    fun removeContents(actionItem: ActionItem, update: Boolean = true): Int {
        val contentsAmount = contents.count(actionItem::equals)
        contents.remove(actionItem)
        if(update) { update() }
        return contentsAmount
    }

    fun removeContents(itemStack: ItemStack, update: Boolean = true): Int {
        val contentsAmount: Int
        contents.filter { itemStack==it.itemStack }.apply {
            contentsAmount = this.size
            forEach(contents::remove)
        }
        if(update) { update() }
        return contentsAmount
    }

    fun clearContents(): CustomContentsSyncGui {
        contents.clear()
        return this
    }

    override fun open(player: Player) {
        open(player, page, SORT_BY_DISPLAY_NAME)
    }

    fun <T> open(player: Player, page: Int, sort: (ActionItem)->Comparable<T>?) {
        this.sort = sort
        update(page)
        player.openInventory(openInventory)
        val listener = object: CustomGuiEvents() {
            override val uniqueInventoryHolder: UniqueInventoryHolder = openInventoryHolder
            override val javaPlugin = plugin
            override fun onInventoryClick(event: InventoryClickEvent) {
                this@CustomContentsSyncGui.actionItems.subList(maxOf((this@CustomContentsSyncGui.page-1)*index,0),
                    maxOf(this@CustomContentsSyncGui.contents.size,0)
                )
                    .filter { it.itemStack == event.currentItem }
                    .filter { (it.slot?:event.slot) == event.slot }.forEach { actionItem ->
                        if (!actionItem.isAllowGet) {
                            event.isCancelled = true
                        }
                        actionItem.getActions().filter { it.key == event.click }.forEach {
                            it.value.invoke(event.asCustomGuiClickEvent(this@CustomContentsSyncGui))
                        }
                    }
            }
            override fun onInventoryClose(event: InventoryCloseEvent) {
                if(isSingleton) { super.onInventoryClose(event) }
            }

            override fun onPlayerQuit(event: PlayerQuitEvent) {
                if(isSingleton) { super.onPlayerQuit(event) }
            }
        }
        CustomGuiEventListener.register(listener)
    }

    override fun clone(): CustomContentsSyncGui {
        val customContentsGui = CustomContentsSyncGui(plugin, size, title, startX, startY, endX, endY, runnable)
        for(x in 0..8) { for(y in 0..size) {
            customContentsGui.setItem(x, y, this.getItem(x, y)?.clone()?:continue)
        } }
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

        val SORT_BY_DISPLAY_NAME: (ActionItem)->Comparable<String>? = { it.itemStack.itemMeta?.displayName?:it.itemStack.type.toString() }
        val SORT_BY_DATE: (ActionItem)->Comparable<Date>? = { it.addDate }
        val SORT_BY_AMOUNT: (ActionItem)->Comparable<Int>? = {it.itemStack.amount}

        fun JavaPlugin.createCustomContentsGui(size: Int, title: String, startX: Int, startY: Int, endX: Int, endY: Int, runnable: CustomContentsSyncGui.() -> Unit = {}): CustomContentsSyncGui {
            return CustomContentsSyncGui(this, size, title, startX, startY, endX, endY, runnable)
        }
    }
}