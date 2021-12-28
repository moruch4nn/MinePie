package dev.moru3.minepie.config

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.jvm.internal.Intrinsics

class Config(private val plugin: Plugin, private val configFile: File) {
    private var configuration: FileConfiguration? = null

    constructor(plugin: Plugin, filename: String): this(plugin, File(plugin.dataFolder, filename))

    fun saveDefaultConfig() { if (!configFile.exists()) plugin.saveResource(configFile.name, false) }

    fun reloadConfig() {
        configuration = YamlConfiguration.loadConfiguration(configFile) as FileConfiguration
        val defaultConfig = plugin.getResource(configFile.name)
        configuration?.defaults = YamlConfiguration.loadConfiguration(InputStreamReader(defaultConfig, StandardCharsets.UTF_8)) as Configuration
    }

    fun config(): FileConfiguration? {
        if (configuration == null) reloadConfig()
        return configuration
    }

    fun saveConfig() {
        if (configuration == null) return
        try {
            Intrinsics.checkNotNull(config())
            config()!!.save(configFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}