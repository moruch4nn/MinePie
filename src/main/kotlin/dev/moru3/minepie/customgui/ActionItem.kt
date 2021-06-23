package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.CustomGuiClickEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

open class ActionItem(val itemStack: ItemStack) {

    private val actions: MutableMap<ClickType, (CustomGuiClickEvent)->Unit> = mutableMapOf()

    var addDate: Long = System.nanoTime()
    private set

    var slot: Int? = null
    private set

    var isAllowGet = false

    fun addAction(clickType: ClickType, runnable: (CustomGuiClickEvent)->Unit) {
        actions[clickType] = runnable
    }

    fun getActions(): Map<ClickType, (CustomGuiClickEvent)->Unit> {
        return actions.toMap()
    }

    constructor(itemStack: ItemStack, addDate: Long = System.nanoTime()) : this(itemStack) { this.addDate = addDate }

    constructor(itemStack: ItemStack, addDate: Long = System.nanoTime(), slot: Int?): this(itemStack, addDate) {
        this.slot = slot
    }

    constructor(itemStack: ItemStack, addDate: Long = System.nanoTime(), slot: Int? = null, actions: MutableMap<ClickType, (CustomGuiClickEvent)->Unit>):
            this(itemStack, addDate, slot) {
                actions.forEach(this.actions::put)
            }

    fun clone(): ActionItem {
        val actionItem = ActionItem(itemStack = itemStack.clone(), addDate = addDate, slot = slot, actions = actions)
        actionItem.isAllowGet = isAllowGet
        return actionItem
    }
}