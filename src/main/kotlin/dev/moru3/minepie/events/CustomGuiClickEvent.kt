package dev.moru3.minepie.events

import dev.moru3.minepie.customgui.ICustomGui
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
    val customGui: ICustomGui
) : InventoryClickEvent(view, type, slot, click, action) {
    companion object {
        fun InventoryClickEvent.asCustomGuiClickEvent(customGui: ICustomGui): CustomGuiClickEvent {
            return CustomGuiClickEvent(this.view, this.slotType, this.slot, this.click, this.action, customGui)
        }
    }
}