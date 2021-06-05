package dev.moru3.minepie.inventory

import dev.moru3.minepie.RunnableType
import org.bukkit.inventory.ItemStack

class ActionItem(val itemStack: ItemStack) {
    private val actions: MutableMap<InventoryClickType, Pair<RunnableType, ()->Unit>> = mutableMapOf()

    fun InventoryClickType.addAction(runnable: ()->Unit) {
        actions[this] = Pair(RunnableType.SYNCHRONIZE,runnable)
    }

    fun InventoryClickType.addAsyncAction(runnable: () -> Unit) {
        actions[this] = Pair(RunnableType.ASYNCHRONOUS,runnable)
    }

    fun getActions(): Map<InventoryClickType, Pair<RunnableType, ()->Unit>> {
        return actions.toMap()
    }
}