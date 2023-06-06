package dev.moru3.minepie.utils

import org.bukkit.inventory.ItemStack

var ItemStack.displayName: String?
    get() = this.itemMeta?.displayName
    set(value) {
        val itemMeta = this.itemMeta
        itemMeta?.setDisplayName(value)
        this.itemMeta = itemMeta
    }

var ItemStack.lore: List<String>?
    get() = this.itemMeta?.lore
    set(value) {
        val itemMeta = this.itemMeta
        itemMeta?.lore = value
        this.itemMeta = itemMeta
    }