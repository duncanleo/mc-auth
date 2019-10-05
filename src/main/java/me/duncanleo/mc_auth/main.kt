package me.duncanleo.mc_auth

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
import org.jetbrains.exposed.sql.transactions.transaction

class App : JavaPlugin(), Listener {
  companion object {
    val usersLocationMap = mutableMapOf<String, Location>()
    val usersMap = mutableMapOf<String, Boolean>()
  }

  override fun onEnable() {
    logger.info("Hello there!")

    server.pluginManager.registerEvents(this, this)
    getCommand("login")?.setExecutor(LoginCommand())
    getCommand("register")?.setExecutor(RegisterCommand())

    saveDefaultConfig()

    Database.connect("jdbc:sqlite:users.db", driver = "org.sqlite.JDBC")
    
    transaction {
      addLogger(StdOutSqlLogger)
      
      SchemaUtils.create(Users)
    }
  }

  @EventHandler
  public fun onPlayerJoin(event: PlayerJoinEvent) {
    val displayName = event.player.displayName

    // Maps
    usersLocationMap[displayName] = event.player.location
    usersMap[displayName] = false

    event.player.sendMessage("Please log in to the server.")

    object: BukkitRunnable() {
      override fun run() {
        if (usersMap[displayName] == false) {
          // Still unregistered
          event.player.kickPlayer("Did not log in/register in time")
        }
      }
    }.runTaskLater(this, 20 * config.getLong("login_timeout_sec", 60))
  }

  @EventHandler
  public fun onPlayerLeave(event: PlayerQuitEvent) {
    val displayName = event.player.displayName
    usersMap.remove(displayName)
    usersLocationMap.remove(displayName)
  }

  @EventHandler
  public fun onPlayerMove(event: PlayerMoveEvent) {
    if (!isAuthenticated(event.player.displayName)) {
      event.isCancelled = true
    }
  }

  private fun isAuthenticated(playerName: String): Boolean {
    return usersMap[playerName] == true
  }
}
