package dev.moru3.minepie.customgui

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.*

class UniqueInventoryHolder(private val inventory: Inventory?): InventoryHolder {
    val uniqueId = UUID.randomUUID()
    override fun getInventory(): Inventory? = inventory
}