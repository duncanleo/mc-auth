package me.duncanleo.mc_auth.commands

import me.duncanleo.mc_auth.App
import me.duncanleo.mc_auth.model.*
import me.duncanleo.mc_auth.util.displayNameStripped
import org.bukkit.command.*
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.crypto.bcrypt.*

class LoginCommand : CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (sender !is Player) {
      sender.sendMessage("This command can only be run by players")
      return true
    }
    if (args.isEmpty()) {
      sender.sendMessage("Not enough arguments")
      return false
    }
    transaction {
      val matchingUsers = Users.select { Users.name eq sender.displayNameStripped }.toList()

      if (matchingUsers.isEmpty()) {
        sender.sendMessage("You do not have an account")
      } else {
        val user = matchingUsers.first()
        val attemptPassword = args.first()
        if (BCryptPasswordEncoder().matches(attemptPassword, user[Users.passwordHash])) {
          val savedLocation = App.usersLocationMap[sender.displayName]
          if (savedLocation != null) {
            sender.teleport(savedLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
          }
          App.usersMap[sender.displayName] = true
          App.usersLocationMap.remove(sender.displayName)
          sender.sendMessage("Logged in!")
        } else {
          sender.sendMessage("Incorrect password")
        }
      }
    }
    return true
  }
}
