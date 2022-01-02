package dev.moru3.minepie.nms

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.jvm.internal.Intrinsics


class NmsUtils {
    companion object {
        private val version = Bukkit.getServer().javaClass.`package`.name.replace(".", ",").split(",")[3]

        fun getNmsClass(className: String): Class<*> {
            return Class.forName("net.minecraft.server.${version}.${className}")
        }

        fun getCraftBukkitClass(className: String): Class<*> {
            return Class.forName("org.bukkit.craftbukkit.${version}.${className}")
        }

        fun Player.asNmsPlayer(): Any {
            return this::class.java.getMethod("getHandle").invoke(this)
        }
        fun Any.asBukkitItemStack(): ItemStack {
            return getCraftBukkitClass("inventory.CraftItemStack")
                .getMethod("asBukkitCopy", getNmsClass("ItemStack"))
                .invoke(null, this) as ItemStack
        }
        fun ItemStack.asNmsItemStack(): Any {
            return getCraftBukkitClass("inventory.CraftItemStack")
                .getMethod("asNMSCopy", ItemStack::class.java)
                .invoke(null, this)
        }
        fun Player.sendPacket(any: Any) {
            val nmsPlayer: Any = this.asNmsPlayer()
            val con = nmsPlayer.javaClass.getField("playerConnection")[nmsPlayer]
            val sendPacket = getNmsClass("PlayerConnection").getMethod("sendPacket", *arrayOf(getNmsClass("Packet")))
            sendPacket.invoke(con, any)
        }
    }
}