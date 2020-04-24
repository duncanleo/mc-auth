package me.duncanleo.mc_auth

import java.io.File
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Location
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.EventHandler
import org.bukkit.command.*
import org.bukkit.scheduler.BukkitRunnable
import org.jetbrains.exposed.sql.*
import me.duncanleo.mc_auth.model.*
import me.duncanleo.mc_auth.commands.*
import me.duncanleo.mc_auth.util.displayNameStripped
import org.bukkit.ChatColor
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.jetbrains.exposed.sql.transactions.transaction

class App : JavaPlugin(), Listener, TabCompleter {
  companion object {
    val usersLocationMap = mutableMapOf<String, Location>()
    val usersMap = mutableMapOf<String, Boolean>()
  }

  override fun onEnable() {
    logger.info("Hello there!")

    server.pluginManager.registerEvents(this, this)
    getCommand("login")?.setExecutor(LoginCommand())
    getCommand("login")?.tabCompleter = this
    getCommand("register")?.setExecutor(ChangePasswordCommand())
    getCommand("register")?.tabCompleter = this
    getCommand("changepw")?.setExecutor(ChangePasswordCommand())
    getCommand("changepw")?.tabCompleter = this

    saveDefaultConfig()

    Database.connect("jdbc:sqlite:${File(dataFolder, "users.db").absolutePath}", driver = "org.sqlite.JDBC")

    transaction {
      addLogger(StdOutSqlLogger)
      
      SchemaUtils.create(Users)
    }
  }

  @EventHandler
  public fun onPlayerJoin(event: PlayerJoinEvent) {
    val displayName = event.player.displayNameStripped

    // Maps
    usersLocationMap[displayName] = event.player.location
    usersMap[displayName] = false

    // Teleport to world spawn first
    event.player.teleport(event.player.world.spawnLocation)

    object: BukkitRunnable() {
      override fun run() {
        event.player.sendMessage("${ChatColor.DARK_AQUA}== AUTHENTICATION REQUIRED ==")
        event.player.sendMessage("${ChatColor.DARK_AQUA}Please either log in to the server with ${ChatColor.AQUA}/login <password>")
        event.player.sendMessage("${ChatColor.DARK_AQUA}or register an account with ${ChatColor.AQUA}/register <password> <confirmPassword>")
      }
    }.runTaskLater(this, 20 * 3)

    object: BukkitRunnable() {
      override fun run() {
        if (usersMap[displayName] == false) {
          // Still unregistered
          event.player.kickPlayer("${ChatColor.DARK_AQUA}Did not log in/register in time")
        }
      }
    }.runTaskLater(this, 20 * config.getLong("login_timeout_sec", 60))
  }

  @EventHandler
  public fun onPlayerLeave(event: PlayerQuitEvent) {
    val displayName = event.player.displayNameStripped
    usersMap.remove(displayName)
    usersLocationMap.remove(displayName)
  }

  @EventHandler
  public fun onPlayerMove(event: PlayerMoveEvent) {
    if (!isAuthenticated(event.player.displayNameStripped)) {
      event.isCancelled = true
    }
  }

  @EventHandler
  fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
    if (!isAuthenticated(event.player.displayNameStripped) && !event.message.startsWith("/login") && !event.message.startsWith("/register")) {
      event.isCancelled = true
    }
  }

  public override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): List<String> {
    return listOf()
  }

  private fun isAuthenticated(playerName: String): Boolean {
    return usersMap[playerName] == true
  }
}
