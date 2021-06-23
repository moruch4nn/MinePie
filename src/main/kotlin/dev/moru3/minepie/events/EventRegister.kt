package dev.moru3.minepie.events

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class EventRegister(val plugin: Plugin, runnable: EventRegister.()->Unit) {
    companion object {
        val dummyListener = object: Listener { /**パス**/ }
        inline fun <reified E: Event> Plugin.registerEvent(p: EventPriority = EventPriority.NORMAL, ic: Boolean = false, crossinline r: E.()->Unit) {
            Bukkit.getPluginManager().registerEvent(E::class.java, dummyListener, p, { _, e -> if(e is E) { r.invoke(e) } }, this, ic)
        }
    }

    inline fun <reified E: Event> register(p: EventPriority = EventPriority.NORMAL, ic: Boolean = false, crossinline r: (E)->Unit) {
        plugin.registerEvent(p, ic, r)
    }

    init {
        runnable.invoke(this)
    }
}