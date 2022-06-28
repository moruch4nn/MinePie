package dev.moru3.minepie.customgui.inventory

import dev.moru3.minepie.customgui.*
import dev.moru3.minepie.item.EasyItem
import dev.moru3.minepie.utils.IgnoreRunnable.Companion.ignoreException
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
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
open class CustomContentsSyncGui(plugin: JavaPlugin, size: Int, title: String, private val startX: Int, private val startY: Int, private val endX: Int, private val endY: Int, private val runnable: CustomContentsSyncGui.() -> Unit = {}): CustomSyncGui(plugin, title, size) {
    private val contents = mutableMapOf<ActionItem,ActionItem.()->Unit>()
    private val bufferInventory = super.asInventory()
    private var isSingleton = true
    private var index: Int = 0
    private val indexes = mutableListOf<Int>()
    val override: Boolean = true
    val filler: ItemStack = EasyItem(Material.BLACK_STAINED_GLASS_PANE, " ")

    var page = 1
        private set(value) { field = maxOf(1,value) }

    private var sort: (ActionItem)->Comparable<*>? = SORT_BY_DISPLAY_NAME

    open fun addContents(itemStack: ItemStack, update: Boolean = false, runnable: ActionItem.() -> Unit = {}): CustomContentsSyncGui {
        addContents(ActionItem(itemStack),update,runnable)
        return this
    }

    fun getContents(): Set<ActionItem> = contents.keys

    fun setAutoClose(bool: Boolean) {
        isSingleton = bool
    }

    open fun addContents(actionItem: ActionItem, update: Boolean = false, runnable: ActionItem.() -> Unit = {}): CustomContentsSyncGui {
        contents[actionItem] = runnable
        if(update) { update() }
        return this
    }

    private fun update(page: Int? = null) {
        index = 0
        indexes.clear()
        for(y in startY..endY) { for(x in startX..endX) {
            if(bufferInventory.getItem(x+(y*9))==null||override) {
                indexes.add(x+(y*9))
                index++
            }
        } }
        page?.also { this.page = minOf(maxOf(it,1),(this.contents.size/index)+1) }
        val contents = this.contents.keys.toMutableList().subList((this.page-1)*index, minOf(maxOf(0,this.contents.size),this.contents.size))
        indexes.forEachIndexed { index2, i ->
            val content = contents.getOrNull(index2)
            val action = content?.let { this.contents[content] }
            if(action==null) {
                super.set(i%9,i/9, content) { }
            } else {
                super.set(i%9,i/9, content, action)
            }
        }
    }

    fun replaceContents(old: ItemStack, new: ItemStack, runnable: ActionItem.() -> Unit = {}): CustomContentsSyncGui {
        repeat(removeContents(old)) { addContents(new, false, runnable::invoke) }
        return this
    }

    fun next() {
        update(page+1)
    }

    fun back() {
        update(page-1)
    }

    fun removeContents(actionItem: ActionItem, update: Boolean = false): Int {
        val contentsAmount = contents.count(actionItem::equals)
        contents.remove(actionItem)
        if(update) { update() }
        return contentsAmount
    }

    fun removeContents(itemStack: ItemStack, update: Boolean = false): Int {
        val contentsAmount: Int
        contents.keys.filter { itemStack==it.itemStack }.apply {
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
        super.open(player)
    }

    override fun clone(): CustomContentsSyncGui {
        val customContentsGui = CustomContentsSyncGui(plugin, size, title, startX, startY, endX, endY, runnable)
        for(x in 0..8) { for(y in 0..size) {
            customContentsGui.set(x, y, this.get(x, y)?.clone()?:continue)
        } }
        contents.forEach { customContentsGui.addContents(it.key,false,it.value) }
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