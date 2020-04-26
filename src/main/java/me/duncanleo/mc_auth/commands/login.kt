package me.duncanleo.mc_auth.commands

import me.duncanleo.mc_auth.App
import me.duncanleo.mc_auth.model.Users
import me.duncanleo.mc_auth.util.displayNameStripped
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class LoginCommand : CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (sender !is Player) {
      sender.sendMessage("This command can only be run by players")
      return true
    }
    if (args.isEmpty()) {
      sender.sendMessage("${ChatColor.DARK_AQUA}Not enough arguments")
      return false
    }
    transaction {
      val matchingUsers = Users.select { Users.name eq sender.displayNameStripped }.toList()

      if (matchingUsers.isEmpty()) {
        sender.sendMessage("${ChatColor.DARK_AQUA}You do not have an account")
      } else {
        val user = matchingUsers.first()
        val attemptPassword = args.first()
        if (BCryptPasswordEncoder().matches(attemptPassword, user[Users.passwordHash])) {
          val savedLocation = App.usersLocationMap[sender.displayNameStripped]
          if (savedLocation != null) {
            sender.teleport(savedLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
          }
          App.usersMap[sender.displayNameStripped] = true
          App.usersLocationMap.remove(sender.displayNameStripped)
          sender.sendMessage("${ChatColor.DARK_AQUA}Logged in!")
        } else {
          sender.sendMessage("${ChatColor.DARK_AQUA}Incorrect password")
        }
      }
    }
    return true
  }
}
