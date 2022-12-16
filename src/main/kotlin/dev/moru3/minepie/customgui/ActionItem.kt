package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.CustomGuiClickEvent
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

open class ActionItem(val uniqueTagKey: NamespacedKey,val itemStack: ItemStack): ItemStack(itemStack) {

    private val actions: MutableMap<ClickType, (CustomGuiClickEvent)->Unit> = mutableMapOf()

    private val uniqueId: String = UUID.randomUUID().toString()

    fun isSimilarTag(itemStack: ItemStack?) = itemStack?.itemMeta?.persistentDataContainer?.get(uniqueTagKey, PersistentDataType.STRING) == uniqueId

    var addDate: Date = Date()
    private set

    var slot: Int? = null
        private set

    var clickSound: Sound = Sound.UI_BUTTON_CLICK

    var isAllowGet = false

    init {
        itemStack.itemMeta = itemStack.itemMeta?.also { meta -> meta.persistentDataContainer.set(uniqueTagKey, PersistentDataType.STRING,uniqueId) }
    }

    fun action(vararg clickType: ClickType, runnable: (CustomGuiClickEvent)->Unit) {
        clickType.forEach {
            actions[it] = runnable
        }
    }

    fun getActions(): Map<ClickType, (CustomGuiClickEvent)->Unit> {
        return actions.toMap()
    }

    constructor(uniqueTagKey: NamespacedKey,itemStack: ItemStack, addDate: Date = Date()) : this(uniqueTagKey,itemStack) { this.addDate = addDate }

    constructor(uniqueTagKey: NamespacedKey,itemStack: ItemStack, addDate: Date = Date(), slot: Int?): this(uniqueTagKey, itemStack, addDate) {
        this.slot = slot
    }

    constructor(uniqueTagKey: NamespacedKey, itemStack: ItemStack, addDate: Date = Date(), slot: Int? = null, actions: MutableMap<ClickType, (CustomGuiClickEvent)->Unit>):
            this(uniqueTagKey, itemStack, addDate, slot) {
                actions.forEach(this.actions::put)
            }

    override fun clone(): ActionItem {
        val actionItem = ActionItem(uniqueTagKey = uniqueTagKey,itemStack = itemStack.clone(), addDate = addDate, slot = slot, actions = actions)
        actionItem.isAllowGet = isAllowGet
        return actionItem
    }

    fun copy(slot: Int?): ActionItem {
        val actionItem = ActionItem(uniqueTagKey = uniqueTagKey,itemStack = itemStack.clone(), addDate = addDate, slot = slot?:this.slot, actions = actions)
        actionItem.isAllowGet = isAllowGet
        return actionItem
    }
}