package dev.moru3.minepie.customgui

import dev.moru3.minepie.events.CustomGuiClickEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ActionItem(val itemStack: ItemStack) {
    private val actions: MutableMap<ClickType, (CustomGuiClickEvent)->Unit> = mutableMapOf()

    fun ClickType.addAction(runnable: (CustomGuiClickEvent)->Unit) {
        actions[this] = runnable
    }
    fun getActions(): Map<ClickType, (CustomGuiClickEvent)->Unit> {
        return actions.toMap()
    }
}