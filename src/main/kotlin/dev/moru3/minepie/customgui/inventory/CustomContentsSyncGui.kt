package dev.moru3.minepie.customgui.inventory

import dev.moru3.minepie.customgui.ActionItem
import dev.moru3.minepie.customgui.ICustomGui
import dev.moru3.minepie.utils.IgnoreRunnable.Companion.ignoreException
import dev.moru3.minepie.utils.Utils.Companion.isNull
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
open class CustomContentsSyncGui(plugin: JavaPlugin, size: Int, title: String, private val startX: Int, private val startY: Int, private val endX: Int, private val endY: Int, private val runnable: CustomContentsSyncGui.() -> Unit = {}): CustomGui(plugin, title, size) {
    private val contents = mutableListOf<ActionItem>()
    private val openInventory = super.asInventory()

    var page = 1
        private set

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
        ActionItem(itemStack.clone()).also(contents::add).also(runnable::invoke)
        return this
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
        page?.also { this.page = it }
        var index: Int = 0
        for(x in startX..endX) { for(y in startY..endY) { if(inventory.getItem(x+(y*9))==null) { index++ } } }
        if(this.page != 1) { if(contents.size<index) { update(this.page - 1) } }
        openInventory.contents = inventory.contents
        for(x in startX..endX) { for(y in startY..endY) {
            if(contents.size < index) { break }
            openInventory.setItem(x+(y*9), contents.getOrNull(x+(y*9))?.itemStack?:continue)
            index++
        } }
    }

    fun replaceContents(old: ItemStack, new: ItemStack, runnable: ActionItem.() -> Unit = {}): CustomContentsSyncGui {
        repeat(removeContents(old)) { addContents(new, false, runnable::invoke) }
        update()
        return this
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

        val SORT_BY_DISPLAY_NAME: (ActionItem)->Comparable<String>? = { it.itemStack.itemMeta.displayName?:it.itemStack.type.toString() }
        val SORT_BY_DATE: (ActionItem)->Comparable<Date>? = { it.addDate }
        val SORT_BY_AMOUNT: (ActionItem)->Comparable<Int>? = {it.itemStack.amount}

        fun JavaPlugin.createCustomContentsGui(size: Int, title: String, startX: Int, startY: Int, endX: Int, endY: Int, runnable: CustomContentsSyncGui.() -> Unit = {}): CustomContentsSyncGui {
            return CustomContentsSyncGui(this, size, title, startX, startY, endX, endY, runnable)
        }
    }
}