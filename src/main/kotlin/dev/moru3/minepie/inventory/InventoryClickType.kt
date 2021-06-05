package dev.moru3.minepie.inventory

import org.bukkit.event.inventory.ClickType

enum class InventoryClickType(val bukkit: ClickType) {
    RIGHT_CLICK(ClickType.RIGHT),
    SHIFT_RIGHT_CLICK(ClickType.SHIFT_RIGHT),
    LEFT_CLICK(ClickType.LEFT),
    SHIFT_LEFT_CLICK(ClickType.SHIFT_LEFT)
}