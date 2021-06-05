package dev.moru3.minepie.events

import dev.moru3.minepie.customgui.CustomGui
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.InventoryView

class CustomGuiClickEvent(
    view: InventoryView,
    type: InventoryType.SlotType,
    slot: Int,
    click: ClickType,
    action: InventoryAction,
    val customGui: CustomGui
) : InventoryClickEvent(view, type, slot, click, action) {
    companion object {
        fun InventoryClickEvent.asCustomGuiClickEvent(customGui: CustomGui): CustomGuiClickEvent {
            return CustomGuiClickEvent(this.view, this.slotType, this.slot, this.click, this.action, customGui)
        }
    }
}