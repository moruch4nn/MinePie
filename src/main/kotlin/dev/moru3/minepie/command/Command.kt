package dev.moru3.minepie.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command(val name: String, val isPlayerOnly: Boolean = false): CommandExecutor {

    val subCommands = mutableMapOf<String, ISubCommand>()

    val permission: String? = null

    val Array<out String>.helpMessage: String
    get() {
        return "$this - TODO"
    }

    var LANG_PLAYER_ONLY = "このコマンドはプレイヤーからのみ実行できます。"
    var LANG_NOT_HAS_PERMISSION = "このコマンドを実行する権限がありません。"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        try {
            if(isPlayerOnly&&sender !is Player) throw BukkitCommandException(LANG_PLAYER_ONLY)
            permission?.takeUnless(sender::hasPermission)
                ?.run { throw BukkitCommandException(LANG_NOT_HAS_PERMISSION) }
            subCommands[args.getOrNull(0)]?.also {
                it.permission?.takeUnless(sender::hasPermission)
                    ?.run { throw BukkitCommandException(LANG_NOT_HAS_PERMISSION) }
                it.execute(sender, command, label, args)
            }?:throw BukkitCommandException(args.helpMessage)
        } catch (e: BukkitCommandException) {
            sender.sendMessage(e.message)
        }
        return true
    }

    init {
        Bukkit.getPluginCommand(name)
            .also{ it?.setExecutor(this) }
    }
}