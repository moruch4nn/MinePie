/*
 * Copyright (c) 2021. moru3_48. All Right Reserved.
 */

package dev.moru3.minepie.customgui.buttons

import dev.moru3.minepie.customgui.ActionItem
import dev.moru3.minepie.customgui.inventory.CustomContentsSyncGui
import dev.moru3.minepie.item.EasyItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class NextPage(customContentsGui: CustomContentsSyncGui, item: ItemStack = EasyItem(Material.ARROW, "${ChatColor.GREEN}次のページ")): ActionItem(customContentsGui.uniqueTagKey,item) {
    init {
        this.isAllowGet = false
        action(ClickType.LEFT) {
            customContentsGui
        }
    }
}