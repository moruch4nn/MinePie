package dev.moru3.minepie.customgui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface ICustomGui<T> {

    val title: String
    val size: Int

    val isSync: Boolean

    /**
     * setItemで追加したアイテムを削除します。
     * @param tempItemStack 削除する対象のアイテム。
     * @param ignoreAmount tempItemStackのamountを無視して削除します。
     * @param limit 削除するアイテムのリミットを設定します。
     * @param sortType アイテムをソートしてから削除します。
     */
    fun removeItem(itemStack: ItemStack): T

    fun removeItem(x: Int, y: Int): T

    /**
     * 指定した場所にアイテムを配置します。
     */
    fun setItem(itemStack: ItemStack, x: Int, y: Int, runnable: ActionItem.() -> Unit = {}): T

    /**
     * 指定された位置のActionItemを返します。
     */
    fun getItem(x: Int, y: Int, runnable: ActionItem.() -> Unit = {}): ActionItem?

    /**
     * 今までに設定したCustomGuiをorg.bukkit.inventory.Inventoryで返します。
     */
    fun asInventory(): Inventory

    /**
     * インベントリを開きます。
     */
    fun open(player: Player)

    enum class SortType {
        AMOUNT,
        DISPLAY_NAME,
        DATE,
    }
}