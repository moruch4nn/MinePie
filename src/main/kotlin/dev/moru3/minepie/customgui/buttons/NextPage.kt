/*
 * Copyright (c) 2021. moru3_48. All Right Reserved.
 */

package dev.moru3.minepie.customgui.buttons

import dev.moru3.minepie.customgui.ActionItem
import dev.moru3.minepie.customgui.inventory.CustomContentsGui
import dev.moru3.minepie.item.Item
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class NextPage(customContentsGui: CustomContentsGui, item: ItemStack = Item(Material.ARROW, "${ChatColor.GREEN}次のページ")): ActionItem(item) {
    init {
        this.isAllowGet = false
        addAction(ClickType.LEFT) {
            customContentsGui
        }
    }
}