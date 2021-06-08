package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.CustomGuiClickEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ActionItem(val itemStack: ItemStack) {
    private val actions: MutableMap<ClickType, (CustomGuiClickEvent)->Unit> = mutableMapOf()
    var addDate: Long = System.nanoTime()
    private set

    var slot: Int? = null
    private set

    var isAllowGet = false

    fun ClickType.addAction(runnable: (CustomGuiClickEvent)->Unit) {
        actions[this] = runnable
    }
    fun getActions(): Map<ClickType, (CustomGuiClickEvent)->Unit> {
        return actions.toMap()
    }

    constructor(itemStack: ItemStack, addDate: Long = System.nanoTime()) : this(itemStack) { this.addDate = addDate }

    constructor(itemStack: ItemStack, addDate: Long = System.nanoTime(), slot: Int?): this(itemStack, addDate) {
        this.slot = slot
    }

    fun clone(): ActionItem {
        val actionItem = ActionItem(itemStack, addDate, slot).apply {
            actions.forEach { it.key.addAction(it.value) }
        }
        actionItem.isAllowGet = isAllowGet
        return actionItem
    }
}