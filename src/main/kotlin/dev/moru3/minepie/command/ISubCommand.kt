package dev.moru3.minepie.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

interface ISubCommand {
    val description: String
    val permission: String?
    fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>)
}