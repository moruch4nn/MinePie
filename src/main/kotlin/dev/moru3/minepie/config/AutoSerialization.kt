package dev.moru3.minepie.config

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.objenesis.ObjenesisStd
import java.lang.reflect.Modifier

abstract class AutoSerialization: ConfigurationSerializable {
    override fun serialize(): MutableMap<String, Any> {
        val fields = this::class.java.declaredFields
            .filterNot { Modifier.isTransient(it.modifiers) }
        val result = mutableMapOf<String, Any>()
        fields.forEach { field ->
            result[field.name.asSnakeCase()] = field.get(this)
        }
        return result
    }

    companion object {
        private val objenesis = ObjenesisStd()
        private val regex = Regex("([a-z])([A-Z]+)")

        @JvmStatic
        fun deserialize(args: Map<String, Any>): Any {
            val clazz = Class.forName(args["=="].toString())
            val fields = this::class.java.declaredFields
                .filterNot { Modifier.isTransient(it.modifiers) }
            val instance = objenesis.newInstance(clazz)
            fields.forEach { field -> field.set(instance, args[field.name.asSnakeCase()]) }
            return instance
        }

        private fun String.asSnakeCase() = this.replace(regex, "$1_$2")
    }
}