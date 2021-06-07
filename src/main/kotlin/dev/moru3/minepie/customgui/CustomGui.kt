package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.CustomGuiClickEvent.Companion.asCustomGuiClickEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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
class CustomGui(private val plugin: Plugin, private val size: Int, private val title: String, val startX: Int, val startY: Int, val endX: Int, val endY: Int, val isSync: Boolean = false, runnable: CustomGui.()->Unit = {}) {
    private val inventory: Inventory
    private val actionItems: MutableList<ActionItem> = mutableListOf()
    private val contents = mutableListOf<ItemStack>()
    private var spaceSize = (endX-startX)*(endY-startY)

    /**
     * アクションアイテムを追加します。start..endの範囲内に収まります。
     */
    fun addContents(itemStack: ItemStack, runnable: ActionItem.() -> Unit = {}) {
        ActionItem(itemStack).also {
            actionItems.add(it)
            contents.add(it.itemStack)
            runnable.invoke(it)
        }
    }

    /**
     * setItemで追加したアイテムを削除します。
     * @param tempItemStack 削除する対象のアイテム。
     * @param ignoreAmount tempItemStackのamountを無視して削除します。
     * @param limit 削除するアイテムのリミットを設定します。
     * @param sortType アイテムをソートしてから削除します。
     */
    fun removeItem(tempItemStack: ItemStack, ignoreAmount: Boolean = false, limit: Int? = null, sortType: SortType? = null) {
        val count = 0
        val itemStack = tempItemStack.clone()
        when(sortType) {
            SortType.AMOUNT -> {
                actionItems.sortedBy { it.itemStack.amount }
            }
            SortType.DISPLAY_NAME -> {
                actionItems.sortedBy { it.itemStack.itemMeta?.displayName?:it.itemStack.type.toString() }
            }
            SortType.DATE -> {
                actionItems.sortedBy(ActionItem::addDate)
            }
            null -> {
                actionItems
            }
        }.filter { it.itemStack == itemStack.also { item ->  if(ignoreAmount) { item.amount = it.itemStack.amount } } }
            .run { if(limit!=null) this.subList(0, limit-1) else this }
                .forEach { actionItem ->
                    val all = inventory.all(actionItem.itemStack)
                    all.forEach {
                        if(limit!=null&&count>=limit) { return }
                        inventory.setItem(it.key, null)
                        actionItems.remove(actionItem)
                        count.inc()
                        spaceSize.inc()
                    }
                }
    }

    /**
     * addContentsで追加したアイテムを削除します。
     * @param tempItemStack 削除する対象のアイテム。
     * @param ignoreAmount tempItemStackのamountを無視して削除します。
     * @param limit 削除するアイテムのリミットを設定します。
     * @param sortType アイテムをソートしてから削除します。
     */
    fun removeContents(tempItemStack: ItemStack, ignoreAmount: Boolean = false, limit: Int? = null, sortType: SortType? = null) {
        val count = 0
        val itemStack = tempItemStack.clone()
        when(sortType) {
            SortType.AMOUNT -> {
                actionItems.sortedBy { it.itemStack.amount }
            }
            SortType.DISPLAY_NAME -> {
                actionItems.sortedBy { it.itemStack.itemMeta?.displayName?:it.itemStack.type.toString() }
            }
            SortType.DATE -> {
                actionItems.sortedBy(ActionItem::addDate)
            }
            null -> {
                actionItems
            }
        }.filter { it.itemStack == itemStack.also { item ->  if(ignoreAmount) { item.amount = it.itemStack.amount } } }
            .run { if(limit!=null) this.subList(0, limit-1) else this }
            .forEach { actionItem ->
                if(limit!=null&&count>=limit) { return }
                contents.remove(actionItem.itemStack)
                actionItems.remove(actionItem)
                count.inc()
                spaceSize.inc()
            }
    }

    /**
     * 指定した場所にアイテムを配置します。
     */
    fun setItem(itemStack: ItemStack, x: Int, y: Int, runnable: ActionItem.() -> Unit = {}) {
        if(x !in 0..8) { throw IndexOutOfBoundsException("size is not in the range of (0..8).") }
        if(y !in 0..5) { throw IndexOutOfBoundsException("size is not in the range of (0..5).") }
        ActionItem(itemStack).also {
            actionItems.filter { item -> item.itemStack==inventory.getItem(x+(y*9)) }.also { item ->
                if(item.size==1) { actionItems.remove(item[0]) }
            }
            actionItems.add(it)
            if(x in startX..endX && y in startY..endX) { inventory.getItem(x+(y*9))?:spaceSize.dec() }
            inventory.setItem(x+(y*9), itemStack.clone())
            runnable.invoke(it)
        }
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
     * インベントリを開きます。
     */
    fun open(player: Player, page: Int = 1) {
        val result = Bukkit.createInventory(null, (size+1)*9, title)
        result.contents = inventory.contents.map(ItemStack::clone).toTypedArray()

    }

    /**
     * initは最後に置いておいてね
     */
    init {
        if(size !in 0..5) { throw IllegalArgumentException("size is not in the range of (0..5).") }
        inventory = Bukkit.createInventory(null, (size+1)*9, title)
        runnable.invoke(this)
    }

    enum class SortType {
        AMOUNT,
        DISPLAY_NAME,
        DATE,
    }

    companion object {
        fun Plugin.createCustomGui(size: Int, title: String, runnable: CustomGui.() -> Unit = {}): CustomGui {
            return CustomGui(this, size, title, 0, 0, 8, 5, false, runnable)
        }
        fun Plugin.createSyncCustomGui(size: Int, title: String, runnable: CustomGui.() -> Unit = {}): CustomGui {
            return CustomGui(this, size, title, 0, 0, 8, 5, true, runnable)
        }
    }
}