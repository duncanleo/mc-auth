package me.duncanleo.mc_auth.commands

import me.duncanleo.mc_auth.model.Users
import me.duncanleo.mc_auth.util.displayNameStripped
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class ChangePasswordCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be run by players")
            return true
        }
        if (args.size < 2) {
            sender.sendMessage("Not enough arguments")
            return false
        }
        val password = args.first()
        val confirmPassword = args[1]
        if (password != confirmPassword) {
            sender.sendMessage("${ChatColor.DARK_AQUA}Passwords do not match!")
            return false
        }
        transaction {
            Users.update({ Users.name eq sender.displayNameStripped }) {
                it[passwordHash] = BCryptPasswordEncoder().encode(password)
            }
            sender.sendMessage("${ChatColor.DARK_AQUA}Password updated!")
        }
        return true
    }
}