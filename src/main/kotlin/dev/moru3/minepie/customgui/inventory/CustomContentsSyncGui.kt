package dev.moru3.minepie.customgui.inventory

import dev.moru3.minepie.customgui.ActionItem
import org.bukkit.plugin.java.JavaPlugin

/**
 * CustomContentsGuiのコンテンツのみを同期します。
 * removeContents,addContents,replaceContentsが同期されます。
 * setItemで設置されたアイテム、ページビューは同期されません。
 */
class CustomContentsSyncGui(plugin: JavaPlugin, size: Int, title: String, private val startX: Int, private val startY: Int, private val endX: Int, private val endY: Int, private val runnable: CustomContentsGui.() -> Unit = {}): CustomContentsGui(plugin, size, title, startX, startY, endX, endY) {
    override fun addContents(actionItem: ActionItem, runnable: ActionItem.() -> Unit): CustomContentsGui {
        addContents(actionItem.itemStack.clone()) {
            actionItem.getActions().forEach(this::addAction)
            runnable.invoke(this)
        }

        return this
    }
}