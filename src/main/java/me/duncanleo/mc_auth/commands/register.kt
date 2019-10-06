package me.duncanleo.mc_auth.commands

import me.duncanleo.mc_auth.model.*
import me.duncanleo.mc_auth.util.displayNameStripped
import org.bukkit.command.*
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.crypto.bcrypt.*

class RegisterCommand : CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
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
      sender.sendMessage("Passwords do not match!")
      return false
    }
    transaction {
      val matchingUsers = Users.select { Users.name eq sender.displayNameStripped }.toList()

      if (!matchingUsers.isEmpty()) {
        sender.sendMessage("Account already registered")
      } else {
        Users.insert {
          it[name] = sender.displayNameStripped
          it[passwordHash] = BCryptPasswordEncoder().encode(password)
        }
        sender.sendMessage("Registered, please login")
      }
    }
    return true
  }
}
