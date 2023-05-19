/*
 * Copyright (c) 2021. moru3_48. All Right Reserved.
 */

package dev.moru3.minepie.item

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

open class EasyItem(
    material: Material,
    displayName: String? = null,
    lore: List<String> = listOf(),
    itemFlags: Set<ItemFlag> = setOf(),
    enchantments: Map<Enchantment, Int> = mapOf(),
    lorePrefix: String? = ChatColor.GRAY.toString(),
    customModelData: Int = 0,
) : ItemStack(material) {
    init {
        this.itemMeta = this.itemMeta?.also { itemMeta ->
            displayName?.also(itemMeta::setDisplayName)
            itemMeta.setCustomModelData(customModelData)
            itemMeta.lore = lore.map { "${lorePrefix?:""}${it}" }
            itemFlags.forEach(itemMeta::addItemFlags)
            enchantments.forEach { itemMeta.addEnchant(it.key, it.value, true) }
        }
    }
}