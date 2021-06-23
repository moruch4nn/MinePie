/*
 * Copyright (c) 2021. moru3_48. All Right Reserved.
 */

package dev.moru3.minepie.item

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class Item(
    material: Material,
    displayName: String? = null,
    lore: List<String> = listOf(),
    itemFlags: Set<ItemFlag> = setOf(),
    enchantments: Map<Enchantment, Int> = mapOf(),
    attributes: Map<Attribute, AttributeModifier> = mapOf()
) : ItemStack(material) {
    init {
        this.itemMeta = this.itemMeta?.also { itemMeta ->
            displayName?.also { str -> itemMeta.setDisplayName(str) }
            itemMeta.lore = lore
            itemFlags.forEach(itemMeta::addItemFlags)
            enchantments.forEach { itemMeta.addEnchant(it.key, it.value, true) }
            attributes.forEach(itemMeta::addAttributeModifier)
        }
    }
}