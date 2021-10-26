package dev.moru3.minepie.config

import org.bukkit.plugin.Plugin
import java.io.File

class Config(val plugin: Plugin, val file: File) {

    init {
        plugin.dataFolder.resolve(file).mkdirs()
        plugin.getResource(file.path)
    }

    companion object {
        fun Plugin.getConfig(relativePath: String): Config {
            return Config(this, File(relativePath))
        }
    }
}