package dev.moru3.minepie.customgui

import dev.moru3.minepie.utils.IgnoreRunnable.Companion.ignoreException
import org.bukkit.entity.Player
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
class CustomContentsGui(private val plugin: Plugin, size: Int, title: String, private val startX: Int, private val startY: Int, private val endX: Int, private val endY: Int, runnable: CustomContentsGui.() -> Unit = {}) : CustomGui(plugin, title, size) {
    private val contents = mutableListOf<ItemStack>()
    private var contentsSize = (endX-startX)*(endY-startY)

    fun addContents(itemStack: ItemStack, runnable: ActionItem.() -> Unit = {}) {
        ActionItem(itemStack).also {
            actionItems.add(it)
            contents.add(it.itemStack)
            runnable.invoke(it)
        }
    }

    /**
     * addContentsで追加したアイテムを削除します。
     * @param tempItemStack 削除する対象のアイテム。
     * @param ignoreAmount tempItemStackのamountを無視して削除します。
     * @param limit 削除するアイテムのリミットを設定します。
     * @param sortType アイテムをソートしてから削除します。
     */
    fun removeContents(tempItemStack: ItemStack, ignoreAmount: Boolean = false, limit: Int? = null, sortType: ICustomGui.SortType? = null) {

    }

    fun clearContents(): CustomContentsGui {
        actionItems.filter { contents.contains(it.itemStack) }.forEach(actionItems::remove)
        contents.clear()
        return this
    }

    override fun setItem(itemStack: ItemStack, x: Int, y: Int, runnable: ActionItem.() -> Unit): CustomGui {
        if(x in startX..endX && y in startY..endY && getItem(x, y) == null) { contentsSize++ }
        return super.setItem(itemStack, x, y, runnable)
    }

    override fun open(player: Player) {
        open(player, 1)
    }

    fun open(player: Player, page: Int = 1) {
        val result = this.asInventory()

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