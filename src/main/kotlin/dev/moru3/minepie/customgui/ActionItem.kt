package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.CustomGuiClickEvent
import org.bukkit.Sound
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.*

open class ActionItem(val itemStack: ItemStack): ItemStack(itemStack) {

    private val actions: MutableMap<ClickType, (CustomGuiClickEvent)->Unit> = mutableMapOf()

    var addDate: Date = Date()
    private set

    var slot: Int? = null
        private set

    var clickSound: Sound = Sound.UI_BUTTON_CLICK

    var isAllowGet = false

    fun addAction(clickType: ClickType, runnable: (CustomGuiClickEvent)->Unit) {
        actions[clickType] = runnable
    }

    fun getActions(): Map<ClickType, (CustomGuiClickEvent)->Unit> {
        return actions.toMap()
    }

    constructor(itemStack: ItemStack, addDate: Date = Date()) : this(itemStack) { this.addDate = addDate }

    constructor(itemStack: ItemStack, addDate: Date = Date(), slot: Int?): this(itemStack, addDate) {
        this.slot = slot
    }

    constructor(itemStack: ItemStack, addDate: Date = Date(), slot: Int? = null, actions: MutableMap<ClickType, (CustomGuiClickEvent)->Unit>):
            this(itemStack, addDate, slot) {
                actions.forEach(this.actions::put)
            }

    override fun clone(): ActionItem {
        val actionItem = ActionItem(itemStack = itemStack.clone(), addDate = addDate, slot = slot, actions = actions)
        actionItem.isAllowGet = isAllowGet
        return actionItem
    }

    fun copy(slot: Int?): ActionItem {
        val actionItem = ActionItem(itemStack = itemStack.clone(), addDate = addDate, slot = slot?:this.slot, actions = actions)
        actionItem.isAllowGet = isAllowGet
        return actionItem
    }
}